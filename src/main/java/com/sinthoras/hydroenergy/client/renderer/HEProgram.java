package com.sinthoras.hydroenergy.client.renderer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.google.common.base.Charsets;
import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.config.HEConfig;

public class HEProgram {

    private static final ResourceLocation vertexShaderLocation = new ResourceLocation(
            HETags.MODID,
            "shader/hewater/shader.vsh");
    private static final ResourceLocation geometryShaderLocation = new ResourceLocation(
            HETags.MODID,
            "shader/hewater/shader.gsh");
    private static final ResourceLocation fragmentShaderLocation = new ResourceLocation(
            HETags.MODID,
            "shader/hewater/shader.fsh");

    private static int programId;
    private static int viewProjectionId;
    private static int waterLevelsId;
    private static int debugStatesId;
    private static int lightLookupTableId;
    private static int atlasTextureId;
    private static int texCoordStillMinId;
    private static int texCoordStillDeltaId;
    private static int texCoordFlowingMinId;
    private static int texCoordFlowingDeltaId;
    private static int fogDiffId;
    private static int fogEndId;
    private static int fogDensityId;
    private static int fogModeLinearId;
    private static int fogColorId;
    private static int cameraPositionId;
    private static int renderOffsetId;

    private static final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer modelviewProjection = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer waterLevels = GLAllocation.createDirectFloatBuffer(HEConfig.maxDams);
    private static final FloatBuffer debugStates = GLAllocation.createDirectFloatBuffer(HEConfig.maxDams);

    private static float fogColorRed = 0;
    private static float fogColorGreen = 0;
    private static float fogColorBlue = 0;

    public static void init() {
        if ((GLContext.getCapabilities().OpenGL32 && !HEConfig.useLimitedRendering) || HEConfig.forceOpenGL) {
            final String defines = "#version 330 core\n" + "#define NUM_CONTROLLERS "
                    + HEConfig.maxDams
                    + "\n"
                    + "#define CLIPPING_OFFSET "
                    + HEConfig.clippingOffset
                    + "\n";
            final int vertexShader = loadShader(vertexShaderLocation, GL20.GL_VERTEX_SHADER, defines);
            final int geometryShader = loadShader(geometryShaderLocation, GL32.GL_GEOMETRY_SHADER, defines);
            final int fragmentShader = loadShader(fragmentShaderLocation, GL20.GL_FRAGMENT_SHADER, defines);

            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vertexShader);
            GL20.glAttachShader(programId, geometryShader);
            GL20.glAttachShader(programId, fragmentShader);
            GL20.glLinkProgram(programId);

            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
                String s = StringUtils.trim(GL20.glGetProgramInfoLog(programId, 32768));
                HE.error("Shader program linking failed: " + s);
                programId = GL31.GL_INVALID_INDEX;
                return;
            }

            viewProjectionId = GL20.glGetUniformLocation(programId, "g_viewProjection");
            waterLevelsId = GL20.glGetUniformLocation(programId, "g_waterLevels");
            debugStatesId = GL20.glGetUniformLocation(programId, "g_debugModes");
            lightLookupTableId = GL20.glGetUniformLocation(programId, "g_lightLUT");
            atlasTextureId = GL20.glGetUniformLocation(programId, "g_atlasTexture");
            texCoordStillMinId = GL20.glGetUniformLocation(programId, "g_texCoordStillMin");
            texCoordStillDeltaId = GL20.glGetUniformLocation(programId, "g_texCoordStillDelta");
            texCoordFlowingMinId = GL20.glGetUniformLocation(programId, "g_texCoordFlowingMin");
            texCoordFlowingDeltaId = GL20.glGetUniformLocation(programId, "g_texCoordFlowingDelta");
            fogDiffId = GL20.glGetUniformLocation(programId, "g_fogDiff");
            fogEndId = GL20.glGetUniformLocation(programId, "g_fogEnd");
            fogDensityId = GL20.glGetUniformLocation(programId, "g_fogDensity");
            fogModeLinearId = GL20.glGetUniformLocation(programId, "g_fogModeLinear");
            fogColorId = GL20.glGetUniformLocation(programId, "g_fogColor");
            cameraPositionId = GL20.glGetUniformLocation(programId, "g_cameraPosition");
            renderOffsetId = GL20.glGetUniformLocation(programId, "g_renderOffset");

            GL20.glUseProgram(0);

            HE.info("Render pipeline initialized.");
        } else {
            HE.info(
                    "Render pipeline NOT initialized. OpenGL 3.2 is not supported! Fallback to vanilla rendering. Expect visual \"anomalies\"...");
        }
    }

    private static int loadShader(ResourceLocation shaderLocation, int type, String defines) {
        try {
            InputStream shaderStream = Minecraft.getMinecraft().getResourceManager().getResource(shaderLocation)
                    .getInputStream();
            BufferedInputStream bufferedinputstream = new BufferedInputStream(shaderStream);
            byte[] shaderBytes = IOUtils.toByteArray(bufferedinputstream);
            byte[] definesBytes = defines.getBytes(Charsets.US_ASCII);
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(shaderBytes.length + definesBytes.length);
            bytebuffer.put(definesBytes);
            bytebuffer.put(shaderBytes);
            bytebuffer.flip();
            final int shaderID = GL20.glCreateShader(type);
            GL20.glShaderSource(shaderID, bytebuffer);
            GL20.glCompileShader(shaderID);
            if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == 0) {
                String s = StringUtils.trim(GL20.glGetShaderInfoLog(shaderID, 32768)); // Const good?
                HE.error("Couldn't compile shader: " + s);
                return GL31.GL_INVALID_INDEX;
            }
            GL20.glUseProgram(programId);
            return shaderID;
        } catch (Exception e) {
            e.printStackTrace();
            return GL31.GL_INVALID_INDEX;
        }
    }

    public static void setViewProjection(float cameraX, float cameraY, float cameraZ) {
        projection.clear();
        modelview.clear();
        modelviewProjection.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
        Matrix4f translation = new Matrix4f().translate(new Vector3f(-cameraX, -cameraY, -cameraZ));
        Matrix4f projectionMatrix = (Matrix4f) new Matrix4f().load(projection.asReadOnlyBuffer());
        Matrix4f modelViewMatrix = (Matrix4f) new Matrix4f().load(modelview.asReadOnlyBuffer());
        Matrix4f result = Matrix4f.mul(modelViewMatrix, translation, null);
        result = Matrix4f.mul(projectionMatrix, result, null);
        result.store(modelviewProjection);
        modelviewProjection.flip();
        GL20.glUniformMatrix4(viewProjectionId, false, modelviewProjection);
    }

    public static void setCameraPosition(float cameraX, float cameraY, float cameraZ) {
        GL20.glUniform3f(cameraPositionId, cameraX, cameraY, cameraZ);
    }

    public static void setWaterLevels() {
        waterLevels.clear();
        waterLevels.put(HEClient.getAllWaterLevelsForRendering());
        waterLevels.flip();
        GL20.glUniform1(waterLevelsId, waterLevels);
    }

    public static void setDebugStates() {
        debugStates.clear();
        debugStates.put(HEClient.getDebugStatesAsFactors());
        debugStates.flip();
        GL20.glUniform1(debugStatesId, debugStates);
    }

    public static void setWaterUV() {
        IIcon iconStill = FluidRegistry.WATER.getStillIcon();
        float minU = iconStill.getMinU();
        float minV = iconStill.getMinV();
        GL20.glUniform2f(texCoordStillMinId, minU, minV);
        GL20.glUniform2f(texCoordStillDeltaId, iconStill.getMaxU() - minU, iconStill.getMaxV() - minV);
        IIcon iconFlowing = FluidRegistry.WATER.getFlowingIcon();
        minU = iconFlowing.getMinU();
        minV = iconFlowing.getMinV();
        GL20.glUniform2f(texCoordFlowingMinId, minU, minV);
        GL20.glUniform2f(texCoordFlowingDeltaId, iconFlowing.getMaxU() - minU, iconFlowing.getMaxV() - minV);
    }

    public static void setCullFronts() {
        GL11.glCullFace(GL11.GL_FRONT);
        GL20.glUniform1f(renderOffsetId, 0.005f);
    }

    public static void setCullBacks() {
        GL11.glCullFace(GL11.GL_BACK);
        GL20.glUniform1f(renderOffsetId, 0.0f);
    }

    // Reads the values from openGL. Sadly, the events EntityViewRenderEvent.FogDensity,
    // EntityViewRenderEvent.RenderFogEvent and EntityViewRenderEvent.FogColors do not
    // provide information for all possible states
    public static void setFog() {
        float fogStart = GL11.glGetFloat(GL11.GL_FOG_START);
        float fogEnd = GL11.glGetFloat(GL11.GL_FOG_END);
        GL20.glUniform1f(fogDiffId, fogEnd - fogStart);
        GL20.glUniform1f(fogEndId, fogEnd);

        GL20.glUniform1f(fogDensityId, GL11.glGetFloat(GL11.GL_FOG_DENSITY));

        GL20.glUniform1f(fogModeLinearId, GL11.glGetFloat(GL11.GL_FOG_MODE) == GL11.GL_LINEAR ? 1.0f : 0.0f);

        GL20.glUniform3f(fogColorId, fogColorRed, fogColorGreen, fogColorBlue);
    }

    public static void setFogColor(float red, float green, float blue) {
        fogColorRed = red;
        fogColorGreen = green;
        fogColorBlue = blue;
    }

    public static void bindLightLookupTable() {
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        Minecraft.getMinecraft().getTextureManager()
                .bindTexture(Minecraft.getMinecraft().entityRenderer.locationLightMap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL20.glUniform1i(lightLookupTableId, 1);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void bindAtlasTexture() {
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GL20.glUniform1i(atlasTextureId, 2);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void bind() {
        GL20.glUseProgram(programId);
    }

    public static void unbind() {
        GL20.glUseProgram(0);
    }
}
