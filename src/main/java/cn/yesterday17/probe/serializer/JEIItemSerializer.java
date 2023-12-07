package cn.yesterday17.probe.serializer;

import cn.yesterday17.probe.Probe;
import cn.yesterday17.probe.ZSRCFile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Objects;

public class JEIItemSerializer implements JsonSerializer<ZSRCFile.JEIItem> {
    @Override
    public JsonElement serialize(ZSRCFile.JEIItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject ingredient = new JsonObject();
        ItemStack stack = src.getStack();

        try {
            ingredient.addProperty("name", src.getIngredient().getDisplayName());
            ingredient.addProperty("unlocalizedName", stack.getUnlocalizedName());
            ingredient.addProperty("modName", src.getIngredient().getModNameForSorting());
            ingredient.add("resourceLocation", context.serialize(stack.getItem().getRegistryName(), ResourceLocation.class));
            ingredient.addProperty("metadata", stack.getMetadata());

            ingredient.addProperty("maxStackSize", stack.getMaxStackSize());
            ingredient.addProperty("maxDamage", stack.getMaxDamage());
            ingredient.addProperty("canRepair", stack.getItem().isRepairable());

            ingredient.addProperty("image", stackToBase64Image(stack));
            ingredient.addProperty("nbtTags", Objects.requireNonNull(stack.getItem().getNBTShareTag(stack)).toString());
            ingredient.add("tooltips", context.serialize(src.getIngredient().getTooltipStrings()));
            ingredient.add("creativeTabStrings", context.serialize(src.getIngredient().getCreativeTabsStrings()));
        } catch (Exception e) {
            Probe.logger.error("Failed serializing JEIItems!");
            Probe.logger.error(e, e);
        }

        return ingredient;
    }

    private static String stackToBase64Image(ItemStack stack) {
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        TextureAtlasSprite texture = model.getParticleTexture();
        int[][] frameTextureData = texture.getFrameTextureData(0);

        if (frameTextureData.length > 0) {
            BufferedImage image = new BufferedImage(texture.getIconWidth(), texture.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

            int[] pixelData = frameTextureData[0];
            int pixelIndex = 0;
            for (int y = 0; y < texture.getIconHeight(); y++) {
                for (int x = 0; x < texture.getIconWidth(); x++) {
                    int argb = pixelData[pixelIndex++];
                    image.setRGB(x, y, argb);
                }
            }
            // Convert BufferedImage to base64 string
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                return Base64.getEncoder().encodeToString(imageBytes);
            } catch (IOException e) {
                Probe.logger.error("Error when trying to convert item texture to base64.");
                Probe.logger.error(e, e);
                return "";
            }
        } else {
            Probe.logger.error("The provided stack didn't contain any available model.");
            return "";
        }
    }
}
