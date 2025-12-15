package org.embeddedt.embeddium.impl.gui;

import org.embeddedt.embeddium.impl.Embeddium;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.impl.gui.console.Console;
import org.embeddedt.embeddium.impl.gui.console.message.MessageLevel;
import org.embeddedt.embeddium.api.options.structure.Option;
import org.embeddedt.embeddium.api.options.structure.OptionFlag;
import org.embeddedt.embeddium.api.options.structure.OptionGroup;
import org.embeddedt.embeddium.api.options.structure.OptionPage;
import org.embeddedt.embeddium.api.options.structure.OptionStorage;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.api.math.Dim2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.frame.BasicFrame;
import org.embeddedt.embeddium.impl.gui.frame.components.SearchTextFieldComponent;
import org.embeddedt.embeddium.impl.gui.frame.components.SearchTextFieldModel;
import org.embeddedt.embeddium.impl.gui.frame.tab.Tab;
import org.embeddedt.embeddium.impl.gui.frame.tab.TabFrame;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class EmbeddiumVideoOptionsScreen extends Screen {
    private static final ResourceLocation LOGO_LOCATION = ResourceLocation.fromNamespaceAndPath(Embeddium.MODID, "textures/embeddium/gui/logo_transparent.png");

    private static final AtomicReference<Component> tabFrameSelectedTab = new AtomicReference<>(null);
    private final AtomicReference<Integer> tabFrameScrollBarOffset = new AtomicReference<>(0);
    private final AtomicReference<Integer> optionPageScrollBarOffset = new AtomicReference<>(0);

    private final Screen prevScreen;
    private final List<OptionPage> pages = new ArrayList<>();
    private AbstractFrame frame;
    private FlatButtonWidget applyButton, closeButton, undoButton;

    private Dim2i logoDim;

    private boolean hasPendingChanges;

    private SearchTextFieldComponent searchTextField;
    private final SearchTextFieldModel searchTextModel;

    private boolean firstInit = true;

    public EmbeddiumVideoOptionsScreen(Screen prev, List<OptionPage> pages) {
        super(Component.literal("Embeddium Options"));
        this.prevScreen = prev;
        this.pages.addAll(pages);
        this.searchTextModel = new SearchTextFieldModel(this.pages, this);
        registerTextures();
    }

    public static List<OptionPage> makePages() {
        List<OptionPage> pages = new ArrayList<>();

        pages.add(EmbeddiumGameOptionPages.general());
        pages.add(EmbeddiumGameOptionPages.quality());
        pages.add(EmbeddiumGameOptionPages.performance());
        pages.add(EmbeddiumGameOptionPages.advanced());

        OptionGUIConstructionEvent.BUS.post(new OptionGUIConstructionEvent(pages));

        return List.copyOf(pages);
    }

    private void registerTextures() {
        Minecraft.getInstance().getTextureManager().registerAndLoad(LOGO_LOCATION, new SimpleTexture(LOGO_LOCATION));
    }


    public void rebuildUI() {
        // Remember if the search bar was previously focused since we'll lose that information after recreating
        // the widget.
        boolean wasSearchFocused = this.searchTextField.isFocused();
        this.rebuildWidgets();
        if(wasSearchFocused) {
            this.setFocused(this.searchTextField);
        }
    }

    @Override
    protected void init() {
        this.frame = this.parentFrameBuilder().build();
        this.addRenderableWidget(this.frame);

        this.setFocused(this.frame);

        if(firstInit) {
            this.setFocused(this.searchTextField);
            firstInit = false;
        }
    }

    private static final float ASPECT_RATIO = 5f / 4f;
    private static final int MINIMUM_WIDTH = 550;

    protected BasicFrame.Builder parentFrameBuilder() {
        BasicFrame.Builder basicFrameBuilder;

        // Apply aspect ratio clamping on wide enough screens
        int newWidth = this.width;
        if (newWidth > MINIMUM_WIDTH && (float) this.width / (float) this.height > ASPECT_RATIO) {
            newWidth = Math.max(MINIMUM_WIDTH, (int) (this.height * ASPECT_RATIO));
        }

        Dim2i basicFrameDim = new Dim2i((this.width - newWidth) / 2, 0, newWidth, this.height);
        Dim2i tabFrameDim = new Dim2i(basicFrameDim.x() + basicFrameDim.width() / 20 / 2, basicFrameDim.y() + basicFrameDim.height() / 4 / 2, basicFrameDim.width() - (basicFrameDim.width() / 20), basicFrameDim.height() / 4 * 3);

        Dim2i undoButtonDim = new Dim2i(tabFrameDim.getLimitX() - 203, tabFrameDim.getLimitY() + 5, 65, 20);
        Dim2i applyButtonDim = new Dim2i(tabFrameDim.getLimitX() - 134, tabFrameDim.getLimitY() + 5, 65, 20);
        Dim2i closeButtonDim = new Dim2i(tabFrameDim.getLimitX() - 65, tabFrameDim.getLimitY() + 5, 65, 20);

        int logoSizeOnScreen = 20;
        this.logoDim = new Dim2i(tabFrameDim.x(), tabFrameDim.getLimitY() + 25 - logoSizeOnScreen, logoSizeOnScreen, logoSizeOnScreen);

        this.undoButton = new FlatButtonWidget(undoButtonDim, Component.translatable("sodium.options.buttons.undo"), this::undoChanges);
        this.applyButton = new FlatButtonWidget(applyButtonDim, Component.translatable("sodium.options.buttons.apply"), this::applyChanges);
        this.closeButton = new FlatButtonWidget(closeButtonDim, Component.translatable("gui.done"), this::onClose);

        Dim2i searchTextFieldDim = new Dim2i(tabFrameDim.x(), tabFrameDim.y() - 26, tabFrameDim.width(), 20);

        basicFrameBuilder = this.parentBasicFrameBuilder(basicFrameDim, tabFrameDim);

        this.searchTextField = new SearchTextFieldComponent(searchTextFieldDim, this.pages, this.searchTextModel);

        basicFrameBuilder.addChild(dim -> this.searchTextField);

        return basicFrameBuilder;
    }

    private boolean canShowPage(OptionPage page) {
        if(page.getGroups().isEmpty()) {
            return false;
        }

        // Check if any options on this page are visible
        var predicate = searchTextModel.getOptionPredicate();

        for(OptionGroup group : page.getGroups()) {
            for(Option<?> option : group.getOptions()) {
                if(predicate.test(option)) {
                    return true;
                }
            }
        }

        return false;
    }

    private AbstractFrame createTabFrame(Dim2i tabFrameDim) {
        // TabFrame will automatically expand its height to fit all tabs, so the scrollable frame can handle it
        return TabFrame.createBuilder()
                .setDimension(tabFrameDim)
                .shouldRenderOutline(false)
                .setTabSectionScrollBarOffset(tabFrameScrollBarOffset)
                .setTabSectionSelectedTab(tabFrameSelectedTab)
                .addTabs(tabs -> this.pages
                        .stream()
                        .filter(this::canShowPage)
                        .forEach(page -> tabs.put(page.getId().getModId(), Tab.createBuilder().from(page, searchTextModel.getOptionPredicate(), optionPageScrollBarOffset)))
                )
                .onSetTab(() -> {
                    optionPageScrollBarOffset.set(0);
                })
                .build();
    }

    public BasicFrame.Builder parentBasicFrameBuilder(Dim2i parentBasicFrameDim, Dim2i tabFrameDim) {
        return BasicFrame.createBuilder()
                .setDimension(parentBasicFrameDim)
                .shouldRenderOutline(false)
                .addChild(parentDim -> this.createTabFrame(tabFrameDim))
                .addChild(dim -> this.undoButton)
                .addChild(dim -> this.applyButton)
                .addChild(dim -> this.closeButton);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if (this.isInGameUi()) {
            this.renderTransparentBackground(gfx);
        } else {
            if (this.minecraft.level == null) {
                this.renderPanorama(gfx, partialTick);
            }
            this.renderMenuBackground(gfx);
        }

        this.minecraft.gui.renderDeferredSubtitles();

        gfx.blit(
            LOGO_LOCATION,
            this.logoDim.x(),       // x0
            this.logoDim.y(),       // y0
            this.logoDim.x() + this.logoDim.width(),   // x1
            this.logoDim.y() + this.logoDim.height(),  // y1
            0.0f,                     // u0
            1.0f,                     // u1
            0.0f,                     // v0
            1.0f                      // v1
        );
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);
        this.updateControls();
        this.frame.render(drawContext, mouseX, mouseY, delta);
    }

    private void updateControls() {
        boolean hasChanges = this.getAllOptions()
                .anyMatch(Option::hasChanged);

        for (OptionPage page : this.pages) {
            for (Option<?> option : page.getOptions()) {
                if (option.hasChanged()) {
                    hasChanges = true;
                }
            }
        }

        this.applyButton.setEnabled(hasChanges);
        this.undoButton.setVisible(hasChanges);
        this.closeButton.setEnabled(!hasChanges);

        this.hasPendingChanges = hasChanges;
    }

    private Stream<Option<?>> getAllOptions() {
        return this.pages.stream()
                .flatMap(s -> s.getOptions().stream());
    }

    private void applyChanges() {
        final HashSet<OptionStorage<?>> dirtyStorages = new HashSet<>();
        final EnumSet<OptionFlag> flags = EnumSet.noneOf(OptionFlag.class);

        this.getAllOptions().forEach((option -> {
            if (!option.hasChanged()) {
                return;
            }

            option.applyChanges();

            flags.addAll(option.getFlags());
            dirtyStorages.add(option.getStorage());
        }));

        Minecraft client = Minecraft.getInstance();

        if (client.level != null) {
            if (flags.contains(OptionFlag.REQUIRES_RENDERER_RELOAD)) {
                client.levelRenderer.allChanged();
            } else if (flags.contains(OptionFlag.REQUIRES_RENDERER_UPDATE)) {
                client.levelRenderer.needsUpdate();
            }
        }

        if (flags.contains(OptionFlag.REQUIRES_ASSET_RELOAD)) {
            client.updateMaxMipLevel(client.options.mipmapLevels().get());
            client.delayTextureReload();
        }

        if (flags.contains(OptionFlag.REQUIRES_GAME_RESTART)) {
            Console.instance().logMessage(MessageLevel.WARN,
                    Component.translatable("sodium.console.game_restart"), 10.0);
        }

        for (OptionStorage<?> storage : dirtyStorages) {
            storage.save();
        }
    }

    private void undoChanges() {
        this.getAllOptions()
                .forEach(Option::reset);
    }

    @Override
    public boolean keyPressed(KeyEvent Event) {
        if (Event.hasShiftDown() && Event.key() == GLFW.GLFW_KEY_P && !(this.searchTextField != null && this.searchTextField.isFocused())) {
            Minecraft.getInstance().setScreen(new VideoSettingsScreen(this.prevScreen, Minecraft.getInstance(), Minecraft.getInstance().options));

            return true;
        }

        return super.keyPressed(Event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !this.hasPendingChanges;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.prevScreen);
    }
}
