package com.anative.grmillet.hw5_code;

import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by grmillet on 2018-06-21.
 */

class LightParameters {
    int light_on;
    float position[] = new float[4];
    float ambient_color[] = new float[4];
    float diffuse_color[] = new float[4];
    float specular_color[] = new float[4];
    float spot_direction[] = new float[3];
    float spot_exponent;
    float spot_cutoff_angle;
};

class MaterialParameters {
    float ambient_color[] = new float[4];
    float diffuse_color[] = new float[4];
    float specular_color[] = new float[4];
    float emissive_color[] = new float[4];
    float specular_exponent;
}

class LocLightParameter {
    int light_on;
    int position;
    int ambient_color, diffuse_color, specular_color;
    int spot_direction;
    int spot_exponent;
    int spot_cutoff_angle;
    int light_attenuation_factors;
}

class LocMaterialParameter {
    int ambient_color, diffuse_color, specular_color, emissive_color;
    int specular_exponent;
}

public class ShadingProgram extends GLES30Program{

    final static int NUMBER_OF_LIGHT_SUPPORTED = 4;

    int locModelViewProjectionMatrix;
    int locModelViewMatrix;
    int locModelViewMatrixInvTrans;

    int locGlobalAmbientColor;
    LocLightParameter locLight[];
    LocMaterialParameter locMaterial = new LocMaterialParameter();
    int locTexture;
    int locFlagTextureMapping;

    int locFlagFog;
    int locFlagBlind;
    int mFlagTextureMapping;

    LightParameters light[];

    MaterialParameters materialMario = new MaterialParameters();
    MaterialParameters materialBus = new MaterialParameters();
    MaterialParameters materialBike = new MaterialParameters();
    MaterialParameters materialBigAirplane = new MaterialParameters();
    MaterialParameters materialSmallAirplane = new MaterialParameters();
    MaterialParameters materialFloor = new MaterialParameters();

    public ShadingProgram(String vertexShaderCode, String fragmentShaderCode){
        super(vertexShaderCode, fragmentShaderCode);
    }

    /**
     * GLProgram에 결합 된 Shader 내 변수들의 location 인덱스를 설정하는 함수.
     */
    public void prepare() {
        locLight = new LocLightParameter[NUMBER_OF_LIGHT_SUPPORTED];
        for(int i=0 ; i<NUMBER_OF_LIGHT_SUPPORTED ; i++)
            locLight[i] = new LocLightParameter();

        locModelViewProjectionMatrix = GLES30.glGetUniformLocation(mId, "ModelViewProjectionMatrix");
        locModelViewMatrix = GLES30.glGetUniformLocation(mId, "ModelViewMatrix");
        locModelViewMatrixInvTrans = GLES30.glGetUniformLocation(mId, "ModelViewMatrixInvTrans");

        locTexture = GLES30.glGetUniformLocation(mId, "base_texture");

        locFlagTextureMapping = GLES30.glGetUniformLocation(mId, "flag_texture_mapping");
        locFlagFog = GLES30.glGetUniformLocation(mId, "flag_fog");
        locFlagBlind = GLES30.glGetUniformLocation(mId, "flag_blind");
        locGlobalAmbientColor = GLES30.glGetUniformLocation(mId, "global_ambient_color");
        for (int i = 0; i < NUMBER_OF_LIGHT_SUPPORTED; i++) {
            String lightNumStr = "light[" + i + "]";
            locLight[i].light_on = GLES30.glGetUniformLocation(mId, lightNumStr + ".light_on");
            locLight[i].position = GLES30.glGetUniformLocation(mId, lightNumStr + ".position");
            locLight[i].ambient_color = GLES30.glGetUniformLocation(mId, lightNumStr + ".ambient_color");
            locLight[i].diffuse_color = GLES30.glGetUniformLocation(mId, lightNumStr + ".diffuse_color");
            locLight[i].specular_color = GLES30.glGetUniformLocation(mId, lightNumStr + ".specular_color");
            locLight[i].spot_direction = GLES30.glGetUniformLocation(mId, lightNumStr + ".spot_direction");
            locLight[i].spot_exponent = GLES30.glGetUniformLocation(mId, lightNumStr + ".spot_exponent");
            locLight[i].spot_cutoff_angle = GLES30.glGetUniformLocation(mId, lightNumStr + ".spot_cutoff_angle");
            locLight[i].light_attenuation_factors = GLES30.glGetUniformLocation(mId, lightNumStr + ".light_attenuation_factors");
        }

        locMaterial.ambient_color = GLES30.glGetUniformLocation(mId, "material.ambient_color");
        locMaterial.diffuse_color = GLES30.glGetUniformLocation(mId, "material.diffuse_color");
        locMaterial.specular_color = GLES30.glGetUniformLocation(mId, "material.specular_color");
        locMaterial.emissive_color = GLES30.glGetUniformLocation(mId, "material.emissive_color");
        locMaterial.specular_exponent = GLES30.glGetUniformLocation(mId, "material.specular_exponent");
    }

    /**
     * Light와 Material의 값을 설정하는 함수.
     */
    public void initLightsAndMaterial() {
        GLES30.glUseProgram(mId);

        GLES30.glUniform4f(locGlobalAmbientColor, 0.115f, 0.115f, 0.115f, 1.0f);

        for (int i = 0; i < NUMBER_OF_LIGHT_SUPPORTED; i++) {
            GLES30.glUniform1i(locLight[i].light_on, 1); // turn on all lights initially
            GLES30.glUniform4f(locLight[i].position, 0.0f, 0.0f, 1.0f, 0.0f);
            GLES30.glUniform4f(locLight[i].ambient_color, 0.0f, 0.0f, 0.0f, 1.0f);
            if (i == 0) {
                GLES30.glUniform4f(locLight[i].diffuse_color, 1.0f, 1.0f, 1.0f, 1.0f);
                GLES30.glUniform4f(locLight[i].specular_color, 1.0f, 1.0f, 1.0f, 1.0f);
            }
            else {
                GLES30.glUniform4f(locLight[i].diffuse_color, 0.0f, 0.0f, 0.0f, 1.0f);
                GLES30.glUniform4f(locLight[i].specular_color, 0.0f, 0.0f, 0.0f, 1.0f);
            }
            GLES30.glUniform3f(locLight[i].spot_direction, 0.0f, 0.0f, -1.0f);
            GLES30.glUniform1f(locLight[i].spot_exponent, 0.0f); // [0.0, 128.0]
            GLES30.glUniform1f(locLight[i].spot_cutoff_angle, 180.0f); // [0.0, 90.0] or 180.0 (180.0 for no spot light effect)
            GLES30.glUniform4f(locLight[i].light_attenuation_factors, 1.0f, 0.0f, 0.0f, 0.0f); // .w != 0.0f for no ligth attenuation
        }

        GLES30.glUniform4f(locMaterial.ambient_color, 0.2f, 0.2f, 0.2f, 1.0f);
        GLES30.glUniform4f(locMaterial.diffuse_color, 0.8f, 0.8f, 0.8f, 1.0f);
        GLES30.glUniform4f(locMaterial.specular_color, 0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glUniform4f(locMaterial.emissive_color, 0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glUniform1f(locMaterial.specular_exponent, 0.0f); // [0.0, 128.0]

        GLES30.glUseProgram(0);


        // TODO: Material mario.
        materialMario.ambient_color[0] = 0.24725f;
        materialMario.ambient_color[1] = 0.1995f;
        materialMario.ambient_color[2] = 0.0745f;
        materialMario.ambient_color[3] = 1.0f;

        materialMario.diffuse_color[0] = 0.75164f;
        materialMario.diffuse_color[1] = 0.60648f;
        materialMario.diffuse_color[2] = 0.22648f;
        materialMario.diffuse_color[3] = 1.0f;

        materialMario.specular_color[0] = 0.728281f;
        materialMario.specular_color[1] = 0.655802f;
        materialMario.specular_color[2] = 0.466065f;
        materialMario.specular_color[3] = 1.0f;

        materialMario.specular_exponent = 51.2f;

        materialMario.emissive_color[0] = 0.1f;
        materialMario.emissive_color[1] = 0.1f;
        materialMario.emissive_color[2] = 0.0f;
        materialMario.emissive_color[3] = 1.0f;

        // TODO: Material Bus.  - steel blue
        materialBus.ambient_color[0] = 0.27451f;
        materialBus.ambient_color[1] = 0.509804f;
        materialBus.ambient_color[2] = 0.705882f;
        materialBus.ambient_color[3] = 1.0f;

        materialBus.diffuse_color[0] = 0.27451f;
        materialBus.diffuse_color[1] = 0.509804f;
        materialBus.diffuse_color[2] = 0.705882f;
        materialBus.diffuse_color[3] = 1.0f;

        materialBus.specular_color[0] = 0.27451f;
        materialBus.specular_color[1] = 0.509804f;
        materialBus.specular_color[2] = 0.705882f;
        materialBus.specular_color[3] = 1.0f;

        materialBus.specular_exponent = 51.2f;

        materialBus.emissive_color[0] = 0.027451f;
        materialBus.emissive_color[1] = 0.0509804f;
        materialBus.emissive_color[2] = 0.0705882f;
        materialBus.emissive_color[3] = 1.0f;

        // TODO: Material Bike. - Dark Slate Gray
        materialBike.ambient_color[0] = 0.192157f;
        materialBike.ambient_color[1] = 0.309804f;
        materialBike.ambient_color[2] = 0.309804f;
        materialBike.ambient_color[3] = 1.0f;

        materialBike.diffuse_color[0] = 0.192157f;
        materialBike.diffuse_color[1] = 0.309804f;
        materialBike.diffuse_color[2] = 0.309804f;
        materialBike.diffuse_color[3] = 1.0f;

        materialBike.specular_color[0] = 0.192157f;
        materialBike.specular_color[1] = 0.309804f;
        materialBike.specular_color[2] = 0.309804f;
        materialBike.specular_color[3] = 1.0f;

        materialBike.specular_exponent = 51.2f;

        materialBike.emissive_color[0] = 0.0192157f;
        materialBike.emissive_color[1] = 0.0309804f;
        materialBike.emissive_color[2] = 0.0309804f;
        materialBike.emissive_color[3] = 1.0f;

        // TODO: Material BigAirplane - Ivory4
        materialBigAirplane.ambient_color[0] = 0.545098f;
        materialBigAirplane.ambient_color[1] = 0.545098f;
        materialBigAirplane.ambient_color[2] = 0.513725f;
        materialBigAirplane.ambient_color[3] = 1.0f;

        materialBigAirplane.diffuse_color[0] = 0.545098f;
        materialBigAirplane.diffuse_color[1] = 0.545098f;
        materialBigAirplane.diffuse_color[2] = 0.513725f;
        materialBigAirplane.diffuse_color[3] = 1.0f;

        materialBigAirplane.specular_color[0] = 0.545098f;
        materialBigAirplane.specular_color[1] = 0.545098f;
        materialBigAirplane.specular_color[2] = 0.513725f;
        materialBigAirplane.specular_color[3] = 1.0f;

        materialBigAirplane.specular_exponent = 51.2f;

        materialBigAirplane.emissive_color[0] = 0.0545098f;
        materialBigAirplane.emissive_color[1] = 0.0545098f;
        materialBigAirplane.emissive_color[2] = 0.0513725f;
        materialBigAirplane.emissive_color[3] = 1.0f;

        // TODO: Material SmallAirplane - Brown
        materialSmallAirplane.ambient_color[0] = 0.647059f;
        materialSmallAirplane.ambient_color[1] = 0.164706f;
        materialSmallAirplane.ambient_color[2] = 0.164706f;
        materialSmallAirplane.ambient_color[3] = 1.0f;

        materialSmallAirplane.diffuse_color[0] = 0.647059f;
        materialSmallAirplane.diffuse_color[1] = 0.164706f;
        materialSmallAirplane.diffuse_color[2] = 0.164706f;
        materialSmallAirplane.diffuse_color[3] = 1.0f;

        materialSmallAirplane.specular_color[0] = 0.647059f;
        materialSmallAirplane.specular_color[1] = 0.164706f;
        materialSmallAirplane.specular_color[2] = 0.164706f;
        materialSmallAirplane.specular_color[3] = 1.0f;

        materialSmallAirplane.specular_exponent = 51.2f;

        materialSmallAirplane.emissive_color[0] = 0.0647059f;
        materialSmallAirplane.emissive_color[1] = 0.0164706f;
        materialSmallAirplane.emissive_color[2] = 0.0164706f;
        materialSmallAirplane.emissive_color[3] = 1.0f;
    }

    public void initFlags() {

        mFlagTextureMapping = 1;

        GLES30.glUseProgram(mId);
        GLES30.glUniform1i(locFlagTextureMapping, mFlagTextureMapping);
        GLES30.glUseProgram(0);
    }

    public void toogle_fog(int flag) {
        GLES30.glUseProgram(mId);
        GLES30.glUniform1i(locFlagFog, flag);
        GLES30.glUseProgram(0);
    }

    public void toogle_blind(int flag) {
        GLES30.glUseProgram(mId);
        GLES30.glUniform1i(locFlagBlind, flag);
        GLES30.glUseProgram(0);
    }

    public void toogle_texture(int flag) {
        GLES30.glUseProgram(mId);
        GLES30.glUniform1i(locFlagTextureMapping, flag);
        GLES30.glUseProgram(0);
    }

    public void blink_effect() {
        for (int i = 0; i<NUMBER_OF_LIGHT_SUPPORTED; i++) {
            light[i].light_on = 1 - light[i].light_on;
        }
    }
    public void set_up_scene_lights(float[] viewMatrix) {

        GLES30.glUseProgram(mId);

        light = new LightParameters[NUMBER_OF_LIGHT_SUPPORTED];

        for(int i=0 ; i<NUMBER_OF_LIGHT_SUPPORTED ; i++)
            light[i] = new LightParameters();

        // TODO: point_light_EC - Light 0

        light[0].light_on = 1;
        light[0].position[0] = 0.0f; light[0].position[1] = 100.0f; light[0].position[2] = 0.0f; light[0].position[3] = 1.0f;

        light[0].ambient_color[0] = 0.13f; light[0].ambient_color[1] = 0.13f; light[0].ambient_color[2] = 0.13f; light[0].ambient_color[3] = 1.0f;
        light[0].diffuse_color[0] = 0.5f; light[0].diffuse_color[1] = 0.5f; light[0].diffuse_color[2] = 0.5f; light[0].diffuse_color[3] = 1.0f;
        light[0].specular_color[0] = 0.8f; light[0].specular_color[1] = 0.8f; light[0].specular_color[2] = 0.8f; light[0].specular_color[3] = 1.0f;
        light[0].spot_direction[0] = 0.0f; light[0].spot_direction[1] = 0.0f; light[0].spot_direction[2] = -1.0f;
        light[0].spot_cutoff_angle = 40.0f;
        light[0].spot_exponent = 8.0f;

        GLES30.glUniform1i(locLight[0].light_on, light[0].light_on);
        GLES30.glUniform4fv(locLight[0].position, 1, BufferConverter.floatArrayToBuffer(light[0].position));
        GLES30.glUniform4fv(locLight[0].ambient_color, 1, BufferConverter.floatArrayToBuffer(light[0].ambient_color));
        GLES30.glUniform4fv(locLight[0].diffuse_color, 1, BufferConverter.floatArrayToBuffer(light[0].diffuse_color));
        GLES30.glUniform4fv(locLight[0].specular_color, 1, BufferConverter.floatArrayToBuffer(light[0].specular_color));

        GLES30.glUniform3fv(locLight[0].spot_direction, 1, BufferConverter.floatArrayToBuffer(light[0].spot_direction));
        GLES30.glUniform1f(locLight[0].spot_cutoff_angle, light[0].spot_cutoff_angle);
        GLES30.glUniform1f(locLight[0].spot_exponent, light[0].spot_exponent);

        // TODO: spot_light_WC - light 1

        light[1].light_on = 1;
        light[1].position[0] = 0.0f; light[1].position[1] = 100.0f; light[1].position[2] = 0.0f; light[1].position[3] = 1.0f;

        light[1].ambient_color[0] = 1.0f; light[1].ambient_color[1] = 0.752941f; light[1].ambient_color[2] = 0.796078f; light[1].ambient_color[3] = 1.0f;
        light[1].diffuse_color[0] = 1.0f; light[1].diffuse_color[1] = 0.752941f; light[1].diffuse_color[2] = 0.796078f; light[1].diffuse_color[3] = 1.0f;
        light[1].specular_color[0] = 1.0f; light[1].specular_color[1] = 0.752941f; light[1].specular_color[2] = 0.796078f; light[1].specular_color[3] = 1.0f;

        light[1].spot_direction[0] = 0.0f; light[1].spot_direction[1] = -1.0f; light[1].spot_direction[2] = 0.0f;
        light[1].spot_cutoff_angle = 30.0f;
        light[1].spot_exponent = 8.0f;


        float[] positionEC = new float[4];
        float[] directionEC = new float[4];
        float[] spot_direction = {light[1].spot_direction[0], light[1].spot_direction[1], light[1].spot_direction[2], 0.0f};

        Matrix.multiplyMV(positionEC, 0, viewMatrix, 0, light[1].position, 0);
        Matrix.multiplyMV(directionEC, 0, viewMatrix, 0, spot_direction, 0);

        GLES30.glUniform1i(locLight[1].light_on, light[1].light_on);
        GLES30.glUniform4fv(locLight[1].position, 1, BufferConverter.floatArrayToBuffer(positionEC));
        GLES30.glUniform4fv(locLight[1].ambient_color, 1, BufferConverter.floatArrayToBuffer(light[1].ambient_color));
        GLES30.glUniform4fv(locLight[1].diffuse_color, 1, BufferConverter.floatArrayToBuffer(light[1].diffuse_color));
        GLES30.glUniform4fv(locLight[1].specular_color, 1, BufferConverter.floatArrayToBuffer(light[1].specular_color));

        GLES30.glUniform3fv(locLight[1].spot_direction, 1, BufferConverter.floatArrayToBuffer(directionEC));
        GLES30.glUniform1f(locLight[1].spot_cutoff_angle, light[1].spot_cutoff_angle);
        GLES30.glUniform1f(locLight[1].spot_exponent, light[1].spot_exponent);


        // TODO: parallel light_EC - Light 3

        light[3].light_on = 1;
        light[3].position[0] = 0.0f; light[3].position[1] = 1000.0f; light[1].position[3] = 0.0f; light[3].position[3] = 0.0f;
        light[3].ambient_color[0] = 0.13f; light[3].ambient_color[1] = 0.13f; light[3].ambient_color[2] = 0.13f; light[3].ambient_color[3] = 1.0f;
        light[3].diffuse_color[0] = 0.13f; light[3].diffuse_color[1] = 0.13f; light[3].diffuse_color[2] = 0.13f; light[3].diffuse_color[3] = 1.0f;
        light[3].specular_color[0] = 0.13f; light[3].specular_color[1] = 0.13f; light[3].specular_color[2] = 0.13f; light[3].specular_color[3] = 1.0f;
        light[3].spot_direction[0] = 0.0f; light[3].spot_direction[1] = -1.0f; light[3].spot_direction[2] = 0.0f;
        light[3].spot_direction[0] = 0.0f; light[3].spot_direction[1] = -1.0f; light[3].spot_direction[2] = 0.0f;
        light[3].spot_cutoff_angle = 180.0f;
        light[3].spot_exponent = 0.0f;


        GLES30.glUniform1i(locLight[3].light_on, light[1].light_on);
        GLES30.glUniform4fv(locLight[3].position, 1, BufferConverter.floatArrayToBuffer(light[3].position));
        GLES30.glUniform4fv(locLight[3].ambient_color, 1, BufferConverter.floatArrayToBuffer(light[3].ambient_color));
        GLES30.glUniform4fv(locLight[3].diffuse_color, 1, BufferConverter.floatArrayToBuffer(light[3].diffuse_color));
        GLES30.glUniform4fv(locLight[3].specular_color, 1, BufferConverter.floatArrayToBuffer(light[3].specular_color));

        GLES30.glUniform3fv(locLight[3].spot_direction, 1, BufferConverter.floatArrayToBuffer(light[3].spot_direction));
        GLES30.glUniform1f(locLight[3].spot_cutoff_angle, light[3].spot_cutoff_angle);
        GLES30.glUniform1f(locLight[3].spot_exponent, light[3].spot_exponent);

        GLES30.glUseProgram(0);
    }

    // TODO: spot_light_MC - Light 2
    public void setUpAirPlaneLight(Airplane airplane, float[] viewMatrix) {

        float[] modelMatrixBigAirplane = new float[16];

        Matrix.setIdentityM(modelMatrixBigAirplane, 0);
        Matrix.rotateM(modelMatrixBigAirplane, 0, airplane.getRotate(), 0f, 1f, 0f);
        Matrix.translateM(modelMatrixBigAirplane, 0, -100.0f, 80.0f, 0.0f);
        Matrix.translateM(modelMatrixBigAirplane, 0, 0.0f, 50.0f, 0.0f);
        Matrix.scaleM(modelMatrixBigAirplane, 0, 1.0f, 1.0f, 1.0f);
        Matrix.multiplyMM(modelMatrixBigAirplane, 0, viewMatrix, 0, modelMatrixBigAirplane, 0);


        light[2].light_on = 1;
        light[2].position[0] = 0.0f; light[2].position[1] = 0.0f; light[2].position[2] = 0.0f; light[2].position[3] = 1.0f;
        light[2].ambient_color[0] = 0.13f; light[2].ambient_color[1] = 0.13f; light[2].ambient_color[2] = 0.13f; light[2].ambient_color[3] = 1.0f;
        light[2].diffuse_color[0] = 1.0f; light[2].diffuse_color[1] = 0.0f; light[2].diffuse_color[2] = 0.0f; light[2].diffuse_color[3] = 1.5f;
        light[2].specular_color[0] = 1.0f; light[2].specular_color[1] = 0.0f; light[2].specular_color[2] = 0.0f; light[2].specular_color[3] = 1.0f;
        light[2].spot_direction[0] = 0.0f; light[2].spot_direction[1] = -1.0f; light[2].spot_direction[2] = 0.0f;
        light[2].spot_cutoff_angle = 20.0f;
        light[2].spot_exponent = 8.0f;

        float[] positionMC = new float[4];
        float[] directionMC = new float[4];
        float[] spot_direction = {light[2].spot_direction[0], light[2].spot_direction[1], light[2].spot_direction[2], 0.0f};

        Matrix.multiplyMV(positionMC, 0, modelMatrixBigAirplane, 0, light[2].position, 0);
        Matrix.multiplyMV(directionMC, 0, modelMatrixBigAirplane, 0, spot_direction, 0);

        GLES30.glUniform1i(locLight[2].light_on, light[2].light_on);
        GLES30.glUniform4fv(locLight[2].position, 1, BufferConverter.floatArrayToBuffer(positionMC));
        GLES30.glUniform4fv(locLight[2].ambient_color, 1, BufferConverter.floatArrayToBuffer(light[2].ambient_color));
        GLES30.glUniform4fv(locLight[2].diffuse_color, 1, BufferConverter.floatArrayToBuffer(light[2].diffuse_color));
        GLES30.glUniform4fv(locLight[2].specular_color, 1, BufferConverter.floatArrayToBuffer(light[2].specular_color));

        GLES30.glUniform3fv(locLight[2].spot_direction, 1, BufferConverter.floatArrayToBuffer(directionMC));
        GLES30.glUniform1f(locLight[2].spot_cutoff_angle, light[2].spot_cutoff_angle);
        GLES30.glUniform1f(locLight[2].spot_exponent, light[2].spot_exponent);
    }

    /* TODO: set up material color */
    public void setUpMaterialMario() {
        GLES30.glUniform4fv(locMaterial.ambient_color, 1, BufferConverter.floatArrayToBuffer(materialMario.ambient_color));
        GLES30.glUniform4fv(locMaterial.diffuse_color, 1, BufferConverter.floatArrayToBuffer(materialMario.diffuse_color));
        GLES30.glUniform4fv(locMaterial.specular_color, 1, BufferConverter.floatArrayToBuffer(materialMario.specular_color));
        GLES30.glUniform1f(locMaterial.specular_exponent, materialMario.specular_exponent);
        GLES30.glUniform4fv(locMaterial.emissive_color, 1, BufferConverter.floatArrayToBuffer(materialMario.emissive_color));
    }

    public void setUpMaterialBus() {
        GLES30.glUniform4fv(locMaterial.ambient_color, 1, BufferConverter.floatArrayToBuffer(materialBus.ambient_color));
        GLES30.glUniform4fv(locMaterial.diffuse_color, 1, BufferConverter.floatArrayToBuffer(materialBus.diffuse_color));
        GLES30.glUniform4fv(locMaterial.specular_color, 1, BufferConverter.floatArrayToBuffer(materialBus.specular_color));
        GLES30.glUniform1f(locMaterial.specular_exponent, materialBus.specular_exponent);
        GLES30.glUniform4fv(locMaterial.emissive_color, 1, BufferConverter.floatArrayToBuffer(materialBus.emissive_color));
    }

    public void setUpMaterialBike() {
        GLES30.glUniform4fv(locMaterial.ambient_color, 1, BufferConverter.floatArrayToBuffer(materialBike.ambient_color));
        GLES30.glUniform4fv(locMaterial.diffuse_color, 1, BufferConverter.floatArrayToBuffer(materialBike.diffuse_color));
        GLES30.glUniform4fv(locMaterial.specular_color, 1, BufferConverter.floatArrayToBuffer(materialBike.specular_color));
        GLES30.glUniform1f(locMaterial.specular_exponent, materialBike.specular_exponent);
        GLES30.glUniform4fv(locMaterial.emissive_color, 1, BufferConverter.floatArrayToBuffer(materialBike.emissive_color));
    }

    public void setUpMaterialBigAirplane() {
        GLES30.glUniform4fv(locMaterial.ambient_color, 1, BufferConverter.floatArrayToBuffer(materialBigAirplane.ambient_color));
        GLES30.glUniform4fv(locMaterial.diffuse_color, 1, BufferConverter.floatArrayToBuffer(materialBigAirplane.diffuse_color));
        GLES30.glUniform4fv(locMaterial.specular_color, 1, BufferConverter.floatArrayToBuffer(materialBigAirplane.specular_color));
        GLES30.glUniform1f(locMaterial.specular_exponent, materialBigAirplane.specular_exponent);
        GLES30.glUniform4fv(locMaterial.emissive_color, 1, BufferConverter.floatArrayToBuffer(materialBigAirplane.emissive_color));
    }

    public void setUpMaterialSmallAirplane() {
        GLES30.glUniform4fv(locMaterial.ambient_color, 1, BufferConverter.floatArrayToBuffer(materialSmallAirplane.ambient_color));
        GLES30.glUniform4fv(locMaterial.diffuse_color, 1, BufferConverter.floatArrayToBuffer(materialSmallAirplane.diffuse_color));
        GLES30.glUniform4fv(locMaterial.specular_color, 1, BufferConverter.floatArrayToBuffer(materialSmallAirplane.specular_color));
        GLES30.glUniform1f(locMaterial.specular_exponent, materialSmallAirplane.specular_exponent);
        GLES30.glUniform4fv(locMaterial.emissive_color, 1, BufferConverter.floatArrayToBuffer(materialSmallAirplane.emissive_color));
    }

    public void setUpMaterialFloor(int flag) {

        switch (flag) {

            case 1:
                // TODO: Material Floor - Forest Green
                materialFloor.ambient_color[0] = 0.133333f;
                materialFloor.ambient_color[1] = 0.545098f;
                materialFloor.ambient_color[2] = 0.133330f;
                materialFloor.ambient_color[3] = 1.0f;

                materialFloor.diffuse_color[0] = 0.133333f;
                materialFloor.diffuse_color[1] = 0.545098f;
                materialFloor.diffuse_color[2] = 0.133330f;
                materialFloor.diffuse_color[3] = 1.0f;

                materialFloor.specular_color[0] = 0.133333f;
                materialFloor.specular_color[1] = 0.545098f;
                materialFloor.specular_color[2] = 0.133330f;
                materialFloor.specular_color[3] = 1.0f;

                materialFloor.specular_exponent = 2.5f;

                materialFloor.emissive_color[0] = 0.0133333f;
                materialFloor.emissive_color[1] = 0.0545098f;
                materialFloor.emissive_color[2] = 0.0133330f;
                materialFloor.emissive_color[3] = 1.0f;
                break;

            case 2:
                // TODO: Material Floor - Floral White
                materialFloor.ambient_color[0] = 1.0f;
                materialFloor.ambient_color[1] = 0.980392f;
                materialFloor.ambient_color[2] = 0.941176f;
                materialFloor.ambient_color[3] = 1.0f;

                materialFloor.diffuse_color[0] = 1.0f;
                materialFloor.diffuse_color[1] = 0.980392f;
                materialFloor.diffuse_color[2] = 0.941176f;
                materialFloor.diffuse_color[3] = 1.0f;

                materialFloor.specular_color[0] = 1.0f;
                materialFloor.specular_color[1] = 0.980392f;
                materialFloor.specular_color[2] = 0.941176f;
                materialFloor.specular_color[3] = 1.0f;

                materialFloor.specular_exponent = 2.5f;

                materialFloor.emissive_color[0] = 0.1f;
                materialFloor.emissive_color[1] = 0.0980392f;
                materialFloor.emissive_color[2] = 0.0941176f;
                materialFloor.emissive_color[3] = 1.0f;
                break;
            case 0:
                // TODO: Material Floor - Burlywood
                materialFloor.ambient_color[0] = 0.870588f;
                materialFloor.ambient_color[1] = 0.721569f;
                materialFloor.ambient_color[2] = 0.529412f;
                materialFloor.ambient_color[3] = 1.0f;

                materialFloor.diffuse_color[0] = 0.870588f;
                materialFloor.diffuse_color[1] = 0.721569f;
                materialFloor.diffuse_color[2] = 0.529412f;
                materialFloor.diffuse_color[3] = 1.0f;

                materialFloor.specular_color[0] = 0.870588f;
                materialFloor.specular_color[1] = 0.721569f;
                materialFloor.specular_color[2] = 0.529412f;
                materialFloor.specular_color[3] = 1.0f;

                materialFloor.specular_exponent = 2.5f;

                materialFloor.emissive_color[0] = 0.0870588f;
                materialFloor.emissive_color[1] = 0.0721569f;
                materialFloor.emissive_color[2] = 0.0529412f;
                materialFloor.emissive_color[3] = 1.0f;
                break;
        }

        GLES30.glUniform4fv(locMaterial.ambient_color, 1, BufferConverter.floatArrayToBuffer(materialFloor.ambient_color));
        GLES30.glUniform4fv(locMaterial.diffuse_color, 1, BufferConverter.floatArrayToBuffer(materialFloor.diffuse_color));
        GLES30.glUniform4fv(locMaterial.specular_color, 1, BufferConverter.floatArrayToBuffer(materialFloor.specular_color));
        GLES30.glUniform1f(locMaterial.specular_exponent, materialFloor.specular_exponent);
        GLES30.glUniform4fv(locMaterial.emissive_color, 1, BufferConverter.floatArrayToBuffer(materialFloor.emissive_color));
    }
}
