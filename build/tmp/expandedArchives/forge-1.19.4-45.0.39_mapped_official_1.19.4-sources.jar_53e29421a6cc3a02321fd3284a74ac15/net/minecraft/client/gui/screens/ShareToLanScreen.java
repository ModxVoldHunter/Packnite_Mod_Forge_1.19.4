package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShareToLanScreen extends Screen {
   private static final int PORT_LOWER_BOUND = 1024;
   private static final int PORT_HIGHER_BOUND = 65535;
   private static final Component ALLOW_COMMANDS_LABEL = Component.translatable("selectWorld.allowCommands");
   private static final Component GAME_MODE_LABEL = Component.translatable("selectWorld.gameMode");
   private static final Component INFO_TEXT = Component.translatable("lanServer.otherPlayers");
   private static final Component PORT_INFO_TEXT = Component.translatable("lanServer.port");
   private static final Component PORT_UNAVAILABLE = Component.translatable("lanServer.port.unavailable.new", 1024, 65535);
   private static final Component INVALID_PORT = Component.translatable("lanServer.port.invalid.new", 1024, 65535);
   private static final int INVALID_PORT_COLOR = 16733525;
   private final Screen lastScreen;
   private GameType gameMode = GameType.SURVIVAL;
   private boolean commands;
   private int port = HttpUtil.getAvailablePort();
   @Nullable
   private EditBox portEdit;

   public ShareToLanScreen(Screen p_96650_) {
      super(Component.translatable("lanServer.title"));
      this.lastScreen = p_96650_;
   }

   protected void init() {
      IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
      this.gameMode = integratedserver.getDefaultGameType();
      this.commands = integratedserver.getWorldData().getAllowCommands();
      this.addRenderableWidget(CycleButton.builder(GameType::getShortDisplayName).withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE).withInitialValue(this.gameMode).create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (p_169429_, p_169430_) -> {
         this.gameMode = p_169430_;
      }));
      this.addRenderableWidget(CycleButton.onOffBuilder(this.commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (p_169432_, p_169433_) -> {
         this.commands = p_169433_;
      }));
      Button button = Button.builder(Component.translatable("lanServer.start"), (p_96660_) -> {
         this.minecraft.setScreen((Screen)null);
         Component component;
         if (integratedserver.publishServer(this.gameMode, this.commands, this.port)) {
            component = PublishCommand.getSuccessMessage(this.port);
         } else {
            component = Component.translatable("commands.publish.failed");
         }

         this.minecraft.gui.getChat().addMessage(component);
         this.minecraft.updateTitle();
      }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build();
      this.portEdit = new EditBox(this.font, this.width / 2 - 75, 160, 150, 20, Component.translatable("lanServer.port"));
      this.portEdit.setResponder((p_258130_) -> {
         Component component = this.tryParsePort(p_258130_);
         this.portEdit.setHint(Component.literal("" + this.port).withStyle(ChatFormatting.DARK_GRAY));
         if (component == null) {
            this.portEdit.setTextColor(14737632);
            this.portEdit.setTooltip((Tooltip)null);
            button.active = true;
         } else {
            this.portEdit.setTextColor(16733525);
            this.portEdit.setTooltip(Tooltip.create(component));
            button.active = false;
         }

      });
      this.portEdit.setHint(Component.literal("" + this.port).withStyle(ChatFormatting.DARK_GRAY));
      this.addRenderableWidget(this.portEdit);
      this.addRenderableWidget(button);
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_96657_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
   }

   public void tick() {
      super.tick();
      if (this.portEdit != null) {
         this.portEdit.tick();
      }

   }

   @Nullable
   private Component tryParsePort(String p_259426_) {
      if (p_259426_.isBlank()) {
         this.port = HttpUtil.getAvailablePort();
         return null;
      } else {
         try {
            this.port = Integer.parseInt(p_259426_);
            if (this.port >= 1024 && this.port <= 65535) {
               return !HttpUtil.isPortAvailable(this.port) ? PORT_UNAVAILABLE : null;
            } else {
               return INVALID_PORT;
            }
         } catch (NumberFormatException numberformatexception) {
            this.port = HttpUtil.getAvailablePort();
            return INVALID_PORT;
         }
      }
   }

   public void render(PoseStack p_96652_, int p_96653_, int p_96654_, float p_96655_) {
      this.renderBackground(p_96652_);
      drawCenteredString(p_96652_, this.font, this.title, this.width / 2, 50, 16777215);
      drawCenteredString(p_96652_, this.font, INFO_TEXT, this.width / 2, 82, 16777215);
      drawCenteredString(p_96652_, this.font, PORT_INFO_TEXT, this.width / 2, 142, 16777215);
      super.render(p_96652_, p_96653_, p_96654_, p_96655_);
   }
}