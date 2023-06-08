package net.minecraft.client.gui.screens.packs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransferableSelectionList extends ObjectSelectionList<TransferableSelectionList.PackEntry> {
   static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
   static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
   static final Component INCOMPATIBLE_CONFIRM_TITLE = Component.translatable("pack.incompatible.confirm.title");
   private final Component title;
   final PackSelectionScreen screen;

   public TransferableSelectionList(Minecraft p_265029_, PackSelectionScreen p_265777_, int p_265774_, int p_265153_, Component p_265124_) {
      super(p_265029_, p_265774_, p_265153_, 32, p_265153_ - 55 + 4, 36);
      this.screen = p_265777_;
      this.title = p_265124_;
      this.centerListVertically = false;
      this.setRenderHeader(true, (int)(9.0F * 1.5F));
   }

   protected void renderHeader(PoseStack p_100063_, int p_100064_, int p_100065_) {
      Component component = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
      this.minecraft.font.draw(p_100063_, component, (float)(p_100064_ + this.width / 2 - this.minecraft.font.width(component) / 2), (float)Math.min(this.y0 + 3, p_100065_), 16777215);
   }

   public int getRowWidth() {
      return this.width;
   }

   protected int getScrollbarPosition() {
      return this.x1 - 6;
   }

   public boolean keyPressed(int p_265499_, int p_265510_, int p_265548_) {
      if (this.getSelected() != null) {
         switch (p_265499_) {
            case 32:
            case 257:
               this.getSelected().keyboardSelection();
               return true;
            default:
               if (Screen.hasShiftDown()) {
                  switch (p_265499_) {
                     case 264:
                        this.getSelected().keyboardMoveDown();
                        return true;
                     case 265:
                        this.getSelected().keyboardMoveUp();
                        return true;
                  }
               }
         }
      }

      return super.keyPressed(p_265499_, p_265510_, p_265548_);
   }

   @OnlyIn(Dist.CLIENT)
   public static class PackEntry extends ObjectSelectionList.Entry<TransferableSelectionList.PackEntry> {
      private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
      private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
      private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
      private static final int ICON_OVERLAY_X_MOVE_UP = 96;
      private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
      private static final int ICON_OVERLAY_Y_SELECTED = 32;
      private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
      private static final int MAX_NAME_WIDTH_PIXELS = 157;
      private static final String TOO_LONG_NAME_SUFFIX = "...";
      private final TransferableSelectionList parent;
      protected final Minecraft minecraft;
      private final PackSelectionModel.Entry pack;
      private final FormattedCharSequence nameDisplayCache;
      private final MultiLineLabel descriptionDisplayCache;
      private final FormattedCharSequence incompatibleNameDisplayCache;
      private final MultiLineLabel incompatibleDescriptionDisplayCache;

      public PackEntry(Minecraft p_265717_, TransferableSelectionList p_265075_, PackSelectionModel.Entry p_265360_) {
         this.minecraft = p_265717_;
         this.pack = p_265360_;
         this.parent = p_265075_;
         this.nameDisplayCache = cacheName(p_265717_, p_265360_.getTitle());
         this.descriptionDisplayCache = cacheDescription(p_265717_, p_265360_.getExtendedDescription());
         this.incompatibleNameDisplayCache = cacheName(p_265717_, TransferableSelectionList.INCOMPATIBLE_TITLE);
         this.incompatibleDescriptionDisplayCache = cacheDescription(p_265717_, p_265360_.getCompatibility().getDescription());
      }

      private static FormattedCharSequence cacheName(Minecraft p_100105_, Component p_100106_) {
         int i = p_100105_.font.width(p_100106_);
         if (i > 157) {
            FormattedText formattedtext = FormattedText.composite(p_100105_.font.substrByWidth(p_100106_, 157 - p_100105_.font.width("...")), FormattedText.of("..."));
            return Language.getInstance().getVisualOrder(formattedtext);
         } else {
            return p_100106_.getVisualOrderText();
         }
      }

      private static MultiLineLabel cacheDescription(Minecraft p_100110_, Component p_100111_) {
         return MultiLineLabel.create(p_100110_.font, p_100111_, 157, 2);
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.pack.getTitle());
      }

      public void render(PoseStack p_100094_, int p_100095_, int p_100096_, int p_100097_, int p_100098_, int p_100099_, int p_100100_, int p_100101_, boolean p_100102_, float p_100103_) {
         PackCompatibility packcompatibility = this.pack.getCompatibility();
         if (!packcompatibility.isCompatible()) {
            GuiComponent.fill(p_100094_, p_100097_ - 1, p_100096_ - 1, p_100097_ + p_100098_ - 9, p_100096_ + p_100099_ + 1, -8978432);
         }

         RenderSystem.setShaderTexture(0, this.pack.getIconTexture());
         GuiComponent.blit(p_100094_, p_100097_, p_100096_, 0.0F, 0.0F, 32, 32, 32, 32);
         FormattedCharSequence formattedcharsequence = this.nameDisplayCache;
         MultiLineLabel multilinelabel = this.descriptionDisplayCache;
         if (this.showHoverOverlay() && (this.minecraft.options.touchscreen().get() || p_100102_ || this.parent.getSelected() == this && this.parent.isFocused())) {
            RenderSystem.setShaderTexture(0, TransferableSelectionList.ICON_OVERLAY_LOCATION);
            GuiComponent.fill(p_100094_, p_100097_, p_100096_, p_100097_ + 32, p_100096_ + 32, -1601138544);
            int i = p_100100_ - p_100097_;
            int j = p_100101_ - p_100096_;
            if (!this.pack.getCompatibility().isCompatible()) {
               formattedcharsequence = this.incompatibleNameDisplayCache;
               multilinelabel = this.incompatibleDescriptionDisplayCache;
            }

            if (this.pack.canSelect()) {
               if (i < 32) {
                  GuiComponent.blit(p_100094_, p_100097_, p_100096_, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  GuiComponent.blit(p_100094_, p_100097_, p_100096_, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            } else {
               if (this.pack.canUnselect()) {
                  if (i < 16) {
                     GuiComponent.blit(p_100094_, p_100097_, p_100096_, 32.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     GuiComponent.blit(p_100094_, p_100097_, p_100096_, 32.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveUp()) {
                  if (i < 32 && i > 16 && j < 16) {
                     GuiComponent.blit(p_100094_, p_100097_, p_100096_, 96.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     GuiComponent.blit(p_100094_, p_100097_, p_100096_, 96.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveDown()) {
                  if (i < 32 && i > 16 && j > 16) {
                     GuiComponent.blit(p_100094_, p_100097_, p_100096_, 64.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     GuiComponent.blit(p_100094_, p_100097_, p_100096_, 64.0F, 0.0F, 32, 32, 256, 256);
                  }
               }
            }
         }

         this.minecraft.font.drawShadow(p_100094_, formattedcharsequence, (float)(p_100097_ + 32 + 2), (float)(p_100096_ + 1), 16777215);
         multilinelabel.renderLeftAligned(p_100094_, p_100097_ + 32 + 2, p_100096_ + 12, 10, 8421504);
      }

      public String getPackId() {
         return this.pack.getId();
      }

      private boolean showHoverOverlay() {
         return !this.pack.isFixedPosition() || !this.pack.isRequired();
      }

      public void keyboardSelection() {
         if (this.pack.canSelect() && this.handlePackSelection()) {
            this.parent.screen.updateFocus(this.parent);
         } else if (this.pack.canUnselect()) {
            this.pack.unselect();
            this.parent.screen.updateFocus(this.parent);
         }

      }

      void keyboardMoveUp() {
         if (this.pack.canMoveUp()) {
            this.pack.moveUp();
         }

      }

      void keyboardMoveDown() {
         if (this.pack.canMoveDown()) {
            this.pack.moveDown();
         }

      }

      private boolean handlePackSelection() {
         if (this.pack.getCompatibility().isCompatible()) {
            this.pack.select();
            return true;
         } else {
            Component component = this.pack.getCompatibility().getConfirmation();
            this.minecraft.setScreen(new ConfirmScreen((p_264693_) -> {
               this.minecraft.setScreen(this.parent.screen);
               if (p_264693_) {
                  this.pack.select();
               }

            }, TransferableSelectionList.INCOMPATIBLE_CONFIRM_TITLE, component));
            return false;
         }
      }

      public boolean mouseClicked(double p_100090_, double p_100091_, int p_100092_) {
         if (p_100092_ != 0) {
            return false;
         } else {
            double d0 = p_100090_ - (double)this.parent.getRowLeft();
            double d1 = p_100091_ - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && d0 <= 32.0D) {
               this.parent.screen.clearSelected();
               if (this.pack.canSelect()) {
                  this.handlePackSelection();
                  return true;
               }

               if (d0 < 16.0D && this.pack.canUnselect()) {
                  this.pack.unselect();
                  return true;
               }

               if (d0 > 16.0D && d1 < 16.0D && this.pack.canMoveUp()) {
                  this.pack.moveUp();
                  return true;
               }

               if (d0 > 16.0D && d1 > 16.0D && this.pack.canMoveDown()) {
                  this.pack.moveDown();
                  return true;
               }
            }

            return false;
         }
      }
   }
}