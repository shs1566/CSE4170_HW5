package com.anative.grmillet.hw5_code;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

public class GLES30Renderer implements GLSurfaceView.Renderer {

    private Context mContext;


    Camera mCamera;

    private Mario mMario;
    private Bus mBus;
    private Bike mBike;
    private Airplane mBigAirplane;
    private Airplane mSmallAirplane1;
    private Airplane mSmallAirplane2;
    private Floor mFloor;


    public float ratio = 1.0f;
    public int headLightFlag = 1;
    public int lampLightFlag = 1;
    public int pointLightFlag = 1;
    public int cowLightFlag = 1;
    public int textureFlag = 1;

    public float[] mMVPMatrix = new float[16];
    public float[] mProjectionMatrix = new float[16];
    public float[] mModelViewMatrix = new float[16];
    public float[] mModelMatrix = new float[16];
    public float[] mViewMatrix = new float[16];
    public float[] mModelViewInvTrans = new float[16];

    final static int TEXTURE_ID_MARIO = 0;
    final static int TEXTURE_ID_IRON = 1;
    final static int TEXTURE_ID_PAPER = 2;
    final static int TEXTURE_ID_GRASS = 3;
    final static  int TEXTURE_ID_WOOD = 4;

    int NUMBER_OF_BIKE = 1;

    private ShadingProgram mShadingProgram;

    public GLES30Renderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        float pos[] = {0.0f, 250.0f, 250.0f};
        float view[] = {0.0f, 0.0f, 0.0f};
        float up[] = {0.0f, 1.0f, 0.0f};
        mCamera = new Camera(pos, view, up);

        mViewMatrix = mCamera.GetViewMatrix();
        //vertex 정보를 할당할 때 사용할 변수.
        int nBytesPerVertex = 8 * 4;        // 3 for vertex, 3 for normal, 2 for texcoord, 4 is sizeof(float)
        int nBytesPerTriangles = nBytesPerVertex * 3;

        /*
            우리가 만든 ShadingProgram을 실제로 생성하는 부분
         */
        mShadingProgram = new ShadingProgram(
            AssetReader.readFromFile("vertexshader.vert" , mContext),
            AssetReader.readFromFile("fragmentshader.frag" , mContext));
        mShadingProgram.prepare();
        mShadingProgram.initLightsAndMaterial();
        mShadingProgram.initFlags();
        mShadingProgram.set_up_scene_lights(mViewMatrix);

        // TODO : load object
        mMario = new Mario();
        mMario.addGeometry(AssetReader.readGeometry("Mario_Triangle.geom", nBytesPerTriangles, mContext));
        mMario.prepare();
        mMario.setTexture(AssetReader.getBitmapFromFile("mario.jpg", mContext), TEXTURE_ID_MARIO);

        mBus = new Bus();
        mBus.addGeometry(AssetReader.readGeometry("Bus.geom", nBytesPerTriangles, mContext));
        mBus.prepare();
        mBus.setTexture(AssetReader.getBitmapFromFile("mario.jpg", mContext), TEXTURE_ID_MARIO);

        mBike = new Bike();
        mBike.addGeometry(AssetReader.readGeometry("Bike.geom", nBytesPerTriangles, mContext));
        mBike.prepare();
        mBike.setTexture(AssetReader.getBitmapFromFile("mario.jpg", mContext), TEXTURE_ID_MARIO);

        mBigAirplane = new Airplane();
        mBigAirplane.addGeometry(AssetReader.readGeometry("airplane.geom", nBytesPerTriangles, mContext));
        mBigAirplane.prepare();
        mBigAirplane.setTexture(AssetReader.getBitmapFromFile("iron.jpg", mContext), TEXTURE_ID_IRON);

        mSmallAirplane1 = new Airplane();
        mSmallAirplane1.addGeometry(AssetReader.readGeometry("airplane.geom", nBytesPerTriangles, mContext));
        mSmallAirplane1.prepare();
        mSmallAirplane1.setTexture(AssetReader.getBitmapFromFile("iron.jpg", mContext), TEXTURE_ID_IRON);

        mSmallAirplane2 = new Airplane();
        mSmallAirplane2.addGeometry(AssetReader.readGeometry("airplane.geom", nBytesPerTriangles, mContext));
        mSmallAirplane2.prepare();
        mSmallAirplane2.setTexture(AssetReader.getBitmapFromFile("iron.jpg", mContext), TEXTURE_ID_IRON);
        mShadingProgram.setUpAirPlaneLight(mBigAirplane, mViewMatrix);

        mFloor = new Floor();
        mFloor.prepare();
        mFloor.setTexture(AssetReader.getBitmapFromFile("grass_tex.jpg", mContext), TEXTURE_ID_GRASS);
        mFloor.setTexture(AssetReader.getBitmapFromFile("paper.jpg", mContext), TEXTURE_ID_PAPER);
        mFloor.setTexture(AssetReader.getBitmapFromFile("wood.jpg", mContext), TEXTURE_ID_WOOD);


    }

    @Override
    public void onDrawFrame(GL10 gl){ // 그리기 함수 ( = display )
        int pid;
        int timestamp = getTimeStamp();

        mViewMatrix = mCamera.GetViewMatrix();

        if (this.lampLightFlag == 1)
            mShadingProgram.toogle_fog(0);
        else
            mShadingProgram.toogle_fog(1);

        if (this.pointLightFlag == 1)
            mShadingProgram.toogle_blind(0);
        else
            mShadingProgram.toogle_blind(1);

        if (this.cowLightFlag == 0)
            mShadingProgram.toogle_texture(0);
        else
            mShadingProgram.toogle_texture(1);


        mShadingProgram.set_up_scene_lights(mViewMatrix);

        NUMBER_OF_BIKE = this.textureFlag;

        Matrix.perspectiveM(mProjectionMatrix, 0, mCamera.getFovy(), ratio, 0.1f, 2000.0f);


        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);


        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.
        pid = mShadingProgram.getProgramID();

        // TODO: draw mario
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 1f, 0f);
        Matrix.scaleM(mModelMatrix, 0, 20.0f, 20.0f, 20.0f);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 0.0f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mMario.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_MARIO);

        mShadingProgram.setUpMaterialMario();
        mMario.draw();

        // TODO: draw bikes
        for (int i=0; i<NUMBER_OF_BIKE; i++) {
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, mBike.getRotate(), 0f, 1f, 0f);
            Matrix.translateM(mModelMatrix, 0, -80.0f + i * -20.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, mBike.getSlide(), -1f, 0f, 0f);
            Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 18.0f);
            Matrix.scaleM(mModelMatrix, 0, 15.0f, 15.0f, 15.0f);

            Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
            Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
            Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

            GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
            GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
            GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mBike.mTexId[0]);
            GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_MARIO);

            mShadingProgram.setUpMaterialBike();
            mBike.draw();
        }

        // TODO: draw airplanes - Hierarchy Modeling
        mShadingProgram.setUpAirPlaneLight(mBigAirplane, mViewMatrix);

        float[] modelMatrixBigAirplane = new float[16];
        float[] modelMatrixSmallAirplane1 = new float[16];
        float[] modelMatrixSnallAirplane2 = new float[16];

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, mBigAirplane.getRotate(), 0f, 1f, 0f);
        Matrix.translateM(mModelMatrix, 0, -100.0f, 80.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, mBigAirplane.getSlide(), 0f, 0f, 1f);
        Matrix.scaleM(mModelMatrix, 0, 1.0f, 1.0f, 1.0f);
        modelMatrixBigAirplane = mModelMatrix;

        Matrix.setIdentityM(modelMatrixSmallAirplane1, 0);
        Matrix.translateM(modelMatrixSmallAirplane1, 0, -40.0f, 0.0f, -30.0f);
        Matrix.scaleM(modelMatrixSmallAirplane1, 0, 0.5f, 0.5f, 0.5f);
        Matrix.multiplyMM(modelMatrixSmallAirplane1, 0, modelMatrixBigAirplane, 0, modelMatrixSmallAirplane1, 0);

        Matrix.setIdentityM(modelMatrixSnallAirplane2, 0);
        Matrix.translateM(modelMatrixSnallAirplane2, 0, 40.0f, 0.0f, -30.0f);
        Matrix.scaleM(modelMatrixSnallAirplane2, 0, 0.5f, 0.5f, 0.5f);
        Matrix.multiplyMM(modelMatrixSnallAirplane2, 0, modelMatrixBigAirplane, 0, modelMatrixSnallAirplane2, 0);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, modelMatrixBigAirplane, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        mShadingProgram.setUpMaterialBigAirplane();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mBigAirplane.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRON);
        mBigAirplane.draw();

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, modelMatrixSmallAirplane1, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        mShadingProgram.setUpMaterialSmallAirplane();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mSmallAirplane1.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRON);
        mSmallAirplane1.draw();

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, modelMatrixSnallAirplane2, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mSmallAirplane2.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRON);
        mSmallAirplane2.draw();

        // TODO: Draw floor
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -250.0f, 0.1f, 250.0f);
        Matrix.rotateM(mModelMatrix, 0, -90.0f, 1f, 0f, 0f);
        Matrix.scaleM(mModelMatrix, 0, 500f, 500f, 500f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFloor.mTexId[0]);

        if (this.headLightFlag == 1)
            GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_GRASS);
        else if (this.headLightFlag == 2)
            GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_PAPER);
        else
            GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_WOOD);
        mShadingProgram.setUpMaterialFloor(this.headLightFlag);
        mFloor.draw(pid);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
        GLES30.glViewport(0, 0, width, height);

        ratio = (float)width / height;

        Matrix.perspectiveM(mProjectionMatrix, 0, mCamera.getFovy(), ratio, 0.1f, 2000.0f);
    }

    static int prevTimeStamp = 0;
    static int currTimeStamp = 0;
    static int totalTimeStamp = 0;

    private int getTimeStamp(){
        Long tsLong = System.currentTimeMillis() / 100;

        currTimeStamp = tsLong.intValue();
        if(prevTimeStamp != 0){
            totalTimeStamp += (currTimeStamp - prevTimeStamp);
        }
        prevTimeStamp = currTimeStamp;


        mBigAirplane.addRotate();
        mBigAirplane.addSlide();

        mBike.addRotate();
        mBike.addSlide();

        return totalTimeStamp;
    }

    public void setLight1(){
        mShadingProgram.light[1].light_on = 1 - mShadingProgram.light[1].light_on;
    }

}