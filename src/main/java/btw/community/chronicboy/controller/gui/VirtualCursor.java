package btw.community.chronicboy.controller.gui;

import btw.community.chronicboy.controller.config.ControllerConfig;
import btw.community.chronicboy.controller.input.ControllerButton;
import btw.community.chronicboy.controller.input.ControllerSnapshot;
import btw.community.chronicboy.controller.mixin.GuiContainerAccessor;
import btw.community.chronicboy.controller.mixin.GuiScreenAccessor;
import emi.dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.src.*;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

public class VirtualCursor {
    // Free-cursor state
    private static float accumulatedX = -1;
    private static float accumulatedY = -1;
    // Smoothed display position (for rendering interpolation)
    private static float smoothX = -1;
    private static float smoothY = -1;
    private static GuiScreen lastScreen = null;
    private static boolean isLeftDown = false;
    private static boolean isRightDown = false;

    // Slot snap state
    private static Slot snappedSlot = null;
    private static boolean wasStickActive = false;

    // Hardware cursor hiding
    private static Cursor emptyCursor = null;
    private static boolean cursorHidden = false;

    public static boolean isLeftDown() {
        return isLeftDown;
    }

    public static boolean isRightDown() {
        return isRightDown;
    }

    // ── Hardware Cursor Management ──────────────────────────────────────

    private static void hideSystemCursor() {
        if (!ControllerConfig.hideHardwareCursor) return;
        if (!Display.isCreated()) return;
        if (emptyCursor == null) {
            try {
                int min = Cursor.getMinCursorSize();
                if (min <= 0) min = 1;
                IntBuffer buffer = ByteBuffer.allocateDirect(min * min * 4)
                        .order(ByteOrder.nativeOrder()).asIntBuffer();
                emptyCursor = new Cursor(min, min, 0, 0, 1, buffer, null);
            } catch (Throwable t) {
                emptyCursor = null;
            }
        }
        if (emptyCursor != null && !cursorHidden) {
            try {
                Mouse.setNativeCursor(emptyCursor);
                cursorHidden = true;
            } catch (Throwable ignored) {}
        }
    }

    public static void showSystemCursor() {
        if (!Display.isCreated()) return;
        if (cursorHidden) {
            try {
                Mouse.setNativeCursor(null);
                cursorHidden = false;
            } catch (Throwable ignored) {}
        }
    }

    // ── Slot Helpers ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static List<Slot> getSlots(GuiContainerAccessor container) {
        Container inv = container.getInventorySlots();
        if (inv == null) return null;
        return (List<Slot>) inv.inventorySlots;
    }

    /**
     * Find the nearest slot within snap range of the cursor.
     */
    private static Slot findNearestSlot(GuiContainerAccessor container, int guiX, int guiY) {
        List<Slot> slots = getSlots(container);
        if (slots == null) return null;

        int guiLeft = container.getGuiLeft();
        int guiTop = container.getGuiTop();

        Slot best = null;
        float bestDist = Float.MAX_VALUE;
        float snapRange = 12.0f;

        for (Slot slot : slots) {
            if (!slot.func_111238_b()) continue;

            float slotCenterX = guiLeft + slot.xDisplayPosition + 8;
            float slotCenterY = guiTop + slot.yDisplayPosition + 8;

            float dx = guiX - slotCenterX;
            float dy = guiY - slotCenterY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < bestDist && dist <= snapRange) {
                bestDist = dist;
                best = slot;
            }
        }

        return best;
    }

    /**
     * Find the nearest slot in a specific direction from the given slot.
     * Used for D-pad one-slot-at-a-time navigation.
     */
    private static Slot findSlotInDirection(GuiContainerAccessor container, Slot current, int dx, int dy) {
        List<Slot> slots = getSlots(container);
        if (slots == null || current == null) return current;

        int cx = current.xDisplayPosition + 8;
        int cy = current.yDisplayPosition + 8;

        Slot best = null;
        float bestScore = Float.MAX_VALUE;

        for (Slot s : slots) {
            if (s == current) continue;
            if (!s.func_111238_b()) continue;

            int sx = s.xDisplayPosition + 8;
            int sy = s.yDisplayPosition + 8;

            int relX = sx - cx;
            int relY = sy - cy;

            // Filter: only consider slots in the pressed direction
            if (dx > 0 && relX <= 0) continue;
            if (dx < 0 && relX >= 0) continue;
            if (dy > 0 && relY <= 0) continue;  // GUI down = +Y
            if (dy < 0 && relY >= 0) continue;  // GUI up = -Y

            // Score: distance in primary direction + heavy penalty for perpendicular offset
            float primary, secondary;
            if (dx != 0) {
                primary = Math.abs(relX);
                secondary = Math.abs(relY);
            } else {
                primary = Math.abs(relY);
                secondary = Math.abs(relX);
            }
            float score = primary + secondary * 3.0f;

            if (score < bestScore) {
                bestScore = score;
                best = s;
            }
        }

        return best != null ? best : current;
    }

    /**
     * Warp the real mouse cursor to the center of a slot.
     */
    private static void warpMouseToSlot(GuiScreen screen, GuiContainerAccessor container, Slot slot) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) return;

        int guiLeft = container.getGuiLeft();
        int guiTop = container.getGuiTop();

        int guiX = guiLeft + slot.xDisplayPosition + 8;
        int guiY = guiTop + slot.yDisplayPosition + 8;

        int pixelX = guiX * mc.displayWidth / screen.width;
        int pixelY = mc.displayHeight - (guiY * mc.displayHeight / screen.height) - 1;

        accumulatedX = pixelX;
        accumulatedY = pixelY;
        // Snap smooth position too for instant visual feedback on D-pad
        smoothX = pixelX;
        smoothY = pixelY;
        Mouse.setCursorPosition(pixelX, pixelY);
    }

    // ── EMI Click Integration ──────────────────────────────────────────

    /**
     * Notify EMI about a click so its buttons, search bar, and sidebar panels respond.
     * EMI normally reads from LWJGL Mouse events which we bypass, so we call directly.
     */
    private static boolean notifyEmiClick(int guiX, int guiY, int button) {
        try {
            return EmiScreenManager.mouseClicked(guiX, guiY, button);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean notifyEmiRelease(int guiX, int guiY, int button) {
        try {
            return EmiScreenManager.mouseReleased(guiX, guiY, button);
        } catch (Throwable ignored) {
            return false;
        }
    }

    // ── Main Update Logic ───────────────────────────────────────────────

    public static void update(GuiScreen screen, ControllerSnapshot controller) {
        if (!Display.isCreated()) return;
        if (screen == null || controller == null || !controller.isConnected) {
            lastScreen = null;
            isLeftDown = false;
            isRightDown = false;
            snappedSlot = null;
            showSystemCursor();
            return;
        }

        if (!Display.isActive()) {
            showSystemCursor();
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) return;

        // Detect screen changes
        if (lastScreen != screen) {
            accumulatedX = Mouse.getX();
            accumulatedY = Mouse.getY();
            smoothX = accumulatedX;
            smoothY = accumulatedY;
            lastScreen = screen;
            isLeftDown = false;
            isRightDown = false;
            snappedSlot = null;
            wasStickActive = false;
        }

        boolean isContainer = screen instanceof GuiContainer && screen instanceof GuiContainerAccessor;
        GuiContainerAccessor container = isContainer ? (GuiContainerAccessor) screen : null;

        // ─── D-pad: Slot-to-slot snapping in containers, pixel nudge in menus ───
        boolean dpadHandled = false;
        if (isContainer && container != null) {
            int dpadDx = 0, dpadDy = 0;
            if (controller.isJustPressed(ControllerButton.DPAD_RIGHT)) dpadDx = 1;
            if (controller.isJustPressed(ControllerButton.DPAD_LEFT)) dpadDx = -1;
            if (controller.isJustPressed(ControllerButton.DPAD_DOWN)) dpadDy = 1;  // down in GUI = +Y
            if (controller.isJustPressed(ControllerButton.DPAD_UP)) dpadDy = -1;   // up in GUI = -Y

            if (dpadDx != 0 || dpadDy != 0) {
                hideSystemCursor();
                dpadHandled = true;

                if (snappedSlot != null) {
                    // Already on a slot: move exactly one slot in direction
                    Slot next = findSlotInDirection(container, snappedSlot, dpadDx, dpadDy);
                    snappedSlot = next;
                    warpMouseToSlot(screen, container, next);
                } else {
                    // Not on a slot: find the nearest one to snap to first
                    int mousePixelX = Mouse.getX();
                    int mousePixelY = Mouse.getY();
                    int guiX = mousePixelX * screen.width / mc.displayWidth;
                    int guiY = screen.height - mousePixelY * screen.height / mc.displayHeight - 1;
                    Slot nearest = findNearestSlot(container, guiX, guiY);
                    if (nearest != null) {
                        // Snap to nearest, then move one step from there
                        Slot next = findSlotInDirection(container, nearest, dpadDx, dpadDy);
                        snappedSlot = next;
                        warpMouseToSlot(screen, container, next);
                    } else {
                        // No nearby slot: find the first visible slot and snap there
                        List<Slot> slots = getSlots(container);
                        if (slots != null && !slots.isEmpty()) {
                            snappedSlot = slots.get(0);
                            warpMouseToSlot(screen, container, snappedSlot);
                        }
                    }
                }
            }
        }

        // ─── Left Stick / D-pad (menus): free cursor movement ───
        float lx = controller.leftStickX;
        float ly = controller.leftStickY;
        boolean hasStickInput = Math.abs(lx) > 0.05f || Math.abs(ly) > 0.05f;

        // D-pad nudge for non-container screens (or if not handled above)
        float nudgeX = 0, nudgeY = 0;
        if (!dpadHandled) {
            float nudgeAmount = 16.0f * (mc.displayWidth / 480.0f);
            if (controller.isJustPressed(ControllerButton.DPAD_UP)) nudgeY += nudgeAmount;
            if (controller.isJustPressed(ControllerButton.DPAD_DOWN)) nudgeY -= nudgeAmount;
            if (controller.isJustPressed(ControllerButton.DPAD_LEFT)) nudgeX -= nudgeAmount;
            if (controller.isJustPressed(ControllerButton.DPAD_RIGHT)) nudgeX += nudgeAmount;
        }
        boolean hasDpadNudge = nudgeX != 0 || nudgeY != 0;

        if (hasStickInput || hasDpadNudge) {
            hideSystemCursor();
            snappedSlot = null; // Un-snap while actively moving

            float speed = ControllerConfig.virtualCursorSpeed * (mc.displayWidth / 360.0f);
            accumulatedX += lx * speed + nudgeX;
            accumulatedY += ly * speed + nudgeY;

            if (accumulatedX < 1) accumulatedX = 1;
            if (accumulatedX > mc.displayWidth - 2) accumulatedX = mc.displayWidth - 2;
            if (accumulatedY < 1) accumulatedY = 1;
            if (accumulatedY > mc.displayHeight - 2) accumulatedY = mc.displayHeight - 2;

            Mouse.setCursorPosition((int) accumulatedX, (int) accumulatedY);
        } else if (!dpadHandled) {
            // Controller at rest: track physical mouse
            int curX = Mouse.getX();
            int curY = Mouse.getY();
            if (Math.abs(curX - accumulatedX) > 2 || Math.abs(curY - accumulatedY) > 2) {
                showSystemCursor();
                snappedSlot = null;
            }
            accumulatedX = curX;
            accumulatedY = curY;
        }

        // ─── Smooth interpolation for rendering ───
        // Snappy lerp (0.20) gives smooth look without feeling sluggish or floaty
        float smoothFactor = 0.20f;
        smoothX += (accumulatedX - smoothX) * (1.0f - smoothFactor);
        smoothY += (accumulatedY - smoothY) * (1.0f - smoothFactor);

        // ─── Magnetic snap when stick is released near a slot ───
        if (isContainer && container != null && !hasStickInput && !dpadHandled) {
            if (wasStickActive) {
                // Stick just released: try to snap
                int mousePixelX = Mouse.getX();
                int mousePixelY = Mouse.getY();
                int guiX = mousePixelX * screen.width / mc.displayWidth;
                int guiY = screen.height - mousePixelY * screen.height / mc.displayHeight - 1;
                Slot nearest = findNearestSlot(container, guiX, guiY);
                if (nearest != null) {
                    snappedSlot = nearest;
                    warpMouseToSlot(screen, container, nearest);
                    hideSystemCursor();
                }
            }
        }

        wasStickActive = hasStickInput;

        // ─── Button Actions ───
        int mousePixelX = Mouse.getX();
        int mousePixelY = Mouse.getY();
        int guiX = mousePixelX * screen.width / mc.displayWidth;
        int guiY = screen.height - mousePixelY * screen.height / mc.displayHeight - 1;

        // A (Cross) = Left Click — works on slots, buttons, EMI widgets, everything
        if (controller.isJustPressed(ControllerButton.A)) {
            isLeftDown = true;
            try {
                // Notify EMI first (handles search bar, sidebar panels, favorites, etc.)
                boolean emiHandled = false;
                if (isContainer) {
                    emiHandled = notifyEmiClick(guiX, guiY, 0);
                }
                // Then pass to the screen's own mouseClicked (handles slots, tabs, buttons)
                if (!emiHandled && screen instanceof GuiScreenAccessor accessor) {
                    accessor.invokeMouseClicked(guiX, guiY, 0);
                }
            } catch (Throwable t) { t.printStackTrace(); }
        } else if (controller.isJustReleased(ControllerButton.A) && isLeftDown) {
            isLeftDown = false;
            try {
                // Notify EMI release
                if (isContainer) {
                    notifyEmiRelease(guiX, guiY, 0);
                }
                if (screen instanceof GuiScreenAccessor accessor) {
                    accessor.invokeMouseMovedOrUp(guiX, guiY, 0);
                }
            } catch (Throwable t) { t.printStackTrace(); }
        }

        // X (Square) = Right Click
        if (controller.isJustPressed(ControllerButton.X)) {
            isRightDown = true;
            try {
                boolean emiHandled = false;
                if (isContainer) {
                    emiHandled = notifyEmiClick(guiX, guiY, 1);
                }
                if (!emiHandled && screen instanceof GuiScreenAccessor accessor) {
                    accessor.invokeMouseClicked(guiX, guiY, 1);
                }
            } catch (Throwable t) { t.printStackTrace(); }
        } else if (controller.isJustReleased(ControllerButton.X) && isRightDown) {
            isRightDown = false;
            try {
                if (isContainer) {
                    notifyEmiRelease(guiX, guiY, 1);
                }
                if (screen instanceof GuiScreenAccessor accessor) {
                    accessor.invokeMouseMovedOrUp(guiX, guiY, 1);
                }
            } catch (Throwable t) { t.printStackTrace(); }
        }

        // Y (Triangle) = Shift-Click on hovered slot (quick move / take 64 from Creative)
        if (isContainer && container != null && controller.isJustPressed(ControllerButton.Y)) {
            Slot hoveredSlot = container.getTheSlot();
            if (hoveredSlot != null) {
                container.invokeHandleMouseClick(hoveredSlot, hoveredSlot.slotNumber, 0, 1);
            }
        }

        // B (Circle) = Back / Escape (returns to previous menu or closes container)
        if (controller.isJustPressed(ControllerButton.B)) {
            showSystemCursor();
            if (screen instanceof GuiScreenAccessor accessor) {
                try {
                    accessor.invokeKeyTyped((char) 27, Keyboard.KEY_ESCAPE);
                } catch (Throwable t) {
                    mc.displayGuiScreen(null);
                    mc.setIngameFocus();
                }
            } else {
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            }
        }

        // Start = Back / Escape
        if (controller.isJustPressed(ControllerButton.START)) {
            showSystemCursor();
            if (screen instanceof GuiScreenAccessor accessor) {
                try {
                    accessor.invokeKeyTyped((char) 27, Keyboard.KEY_ESCAPE);
                } catch (Throwable t) {
                    mc.displayGuiScreen(null);
                    mc.setIngameFocus();
                }
            } else {
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            }
        }
    }

    // ── Rendering ───────────────────────────────────────────────────────

    private static final ResourceLocation XBOX_CURSOR_TEX = new ResourceLocation("controller-ce", "textures/gui/cursor.png");

    public static void render(GuiScreen screen) {
        if (screen == null || !Display.isCreated() || !Display.isActive()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) return;

        try {
            // Use smoothed position for rendering
            float guiX = smoothX * screen.width / mc.displayWidth;
            float guiY = (float) screen.height - smoothY * screen.height / mc.displayHeight - 1;

            // Only use Xbox crosshair in container/inventory screens; circle cursor everywhere else
            boolean isContainer = screen instanceof GuiContainer;
            boolean useXboxCursor = isContainer && "Xbox".equalsIgnoreCase(ControllerConfig.cursorStyle);

            // If snapped to a slot, draw crosshair at slot center
            if (snappedSlot != null && screen instanceof GuiContainerAccessor container) {
                int guiLeft = container.getGuiLeft();
                int guiTop = container.getGuiTop();
                float cx = guiLeft + snappedSlot.xDisplayPosition + 8;
                float cy = guiTop + snappedSlot.yDisplayPosition + 8;

                // Subtle slot highlight box
                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.12f);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(cx - 8, cy - 8);
                GL11.glVertex2f(cx + 8, cy - 8);
                GL11.glVertex2f(cx + 8, cy + 8);
                GL11.glVertex2f(cx - 8, cy + 8);
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();

                if (useXboxCursor) {
                    renderXboxCrosshair(mc, cx, cy);
                } else {
                    renderCircleCursor(cx, cy);
                }
            } else {
                // Free cursor — Xbox crosshair only in containers, circle everywhere else
                if (useXboxCursor) {
                    renderXboxCrosshair(mc, guiX, guiY);
                } else {
                    renderCircleCursor(guiX, guiY);
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Smaller, rounded Xbox-style crosshair drawn in front of everything.
     */
    private static void renderXboxCrosshair(Minecraft mc, float cx, float cy) {
        try {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(GL11.GL_LIGHTING);

            mc.renderEngine.bindTexture(XBOX_CURSOR_TEX);

            // 16x16 texture — half=5 makes it small and non-obtrusive
            float half = 5.0f;
            float z = 300.0f;  // Above everything (items render at z ~200)
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(cx - half, cy + half, z, 0.0, 1.0);
            tess.addVertexWithUV(cx + half, cy + half, z, 1.0, 1.0);
            tess.addVertexWithUV(cx + half, cy - half, z, 1.0, 0.0);
            tess.addVertexWithUV(cx - half, cy - half, z, 0.0, 0.0);
            tess.draw();

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glPopMatrix();
        } catch (Throwable t) {
            renderCircleCursor(cx, cy);
        }
    }

    /**
     * Circle reticle for free cursor mode (non-container screens).
     */
    private static void renderCircleCursor(float guiX, float guiY) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);

        float radius = 4.0f;
        int segments = 24;

        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.65f);
        GL11.glLineWidth(3.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            GL11.glVertex2d(guiX + Math.cos(angle) * (radius + 0.5), guiY + Math.sin(angle) * (radius + 0.5));
        }
        GL11.glEnd();

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.95f);
        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            GL11.glVertex2d(guiX + Math.cos(angle) * radius, guiY + Math.sin(angle) * radius);
        }
        GL11.glEnd();

        GL11.glColor4f(1.0f, 0.2f, 0.2f, 1.0f);
        GL11.glPointSize(3.0f);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex2f(guiX, guiY);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }
}
