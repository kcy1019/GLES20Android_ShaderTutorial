package sponza.OpenGL;

import android.util.*;

public class Camera {

	private float[] pos = new float[3]; // camera position
	private float[] uAxis = new float[3], vAxis = new float[3],
			nAxis = new float[3];
	private float fovy;//
	private final float fovy_MAX = 80.0f, fovy_MIN = 1.0f;

	private final float DELTA_CONSTANT = 0.01f;
	private final float TO_RADIAN = 0.01745329f; // pi/180

	public Camera() {
		pos[0] = 7.0f;
		pos[1] = 0.0f;
		pos[2] = 1.0f;
		uAxis[0] = 0.0f;
		uAxis[1] = 1.0f;
		uAxis[2] = 0.0f;
		vAxis[0] = 0.0f;
		vAxis[1] = 0.0f;
		vAxis[2] = 1.0f;
		nAxis[0] = 1.0f;
		nAxis[1] = 0.0f;
		nAxis[2] = 0.0f;

		fovy = 30.0f;
	}

	public void Pitch(float dPitch) { // rotate u , v->n
		float rad_angle, sina, cosa;

		rad_angle = TO_RADIAN * dPitch * DELTA_CONSTANT * 10.0f;
		sina = FloatMath.sin(rad_angle);
		cosa = FloatMath.cos(rad_angle);
		// calculate v vector
		vAxis[0] = cosa * vAxis[0] + sina
				* (uAxis[1] * vAxis[2] - uAxis[2] * vAxis[1]);
		vAxis[1] = cosa * vAxis[1] + sina
				* (-uAxis[0] * vAxis[2] + uAxis[2] * vAxis[0]);
		vAxis[2] = cosa * vAxis[2] + sina
				* (uAxis[0] * vAxis[1] - uAxis[1] * vAxis[0]);

		nAxis[0] = uAxis[1] * vAxis[2] - uAxis[2] * vAxis[1];
		nAxis[1] = -uAxis[0] * vAxis[2] + uAxis[2] * vAxis[0];
		nAxis[2] = uAxis[0] * vAxis[1] - uAxis[1] * vAxis[0];

		nomalize(vAxis);
		nomalize(nAxis);
	}

	public void Yaw(float dYaw) { // rotate v , n->u
		float rad_angle, sina, cosa;

		rad_angle = TO_RADIAN * dYaw * DELTA_CONSTANT * 10.0f;
		sina = FloatMath.sin(rad_angle);
		cosa = FloatMath.cos(rad_angle);

		nAxis[0] = cosa * nAxis[0] + sina
				* (vAxis[1] * nAxis[2] - vAxis[2] * nAxis[1]);
		nAxis[1] = cosa * nAxis[1] + sina
				* (-vAxis[0] * nAxis[2] + vAxis[2] * nAxis[0]);
		nAxis[2] = cosa * nAxis[2] + sina
				* (vAxis[0] * nAxis[1] - vAxis[1] * nAxis[0]);

		uAxis[0] = vAxis[1] * nAxis[2] - vAxis[2] * nAxis[1];
		uAxis[1] = -vAxis[0] * nAxis[2] + vAxis[2] * nAxis[0];
		uAxis[2] = vAxis[0] * nAxis[1] - vAxis[1] * nAxis[0];

		nomalize(nAxis);
		nomalize(uAxis);

	}

	public void Roll(float dRoll) { // rotate n , u->v
		float rad_angle, sina, cosa;

		rad_angle = TO_RADIAN * dRoll * DELTA_CONSTANT * 10.0f;
		sina = FloatMath.sin(rad_angle);
		cosa = FloatMath.cos(rad_angle);

		uAxis[0] = cosa * uAxis[0] + sina
				* (nAxis[1] * uAxis[2] - nAxis[2] * uAxis[1]);
		uAxis[1] = cosa * uAxis[1] + sina
				* (-nAxis[0] * uAxis[2] + nAxis[2] * uAxis[0]);
		uAxis[2] = cosa * uAxis[2] + sina
				* (nAxis[0] * uAxis[1] - nAxis[1] * uAxis[0]);

		vAxis[0] = nAxis[1] * uAxis[2] - nAxis[2] * uAxis[1];
		vAxis[1] = -nAxis[0] * uAxis[2] + nAxis[2] * uAxis[0];
		vAxis[2] = nAxis[0] * uAxis[1] - nAxis[1] * uAxis[0];

		nomalize(uAxis);
		nomalize(vAxis);

	}

	public void MoveForward(float delta) {
		pos[0] += delta * nAxis[0] * DELTA_CONSTANT;
		pos[1] += delta * nAxis[1] * DELTA_CONSTANT;
		pos[2] += delta * nAxis[2] * DELTA_CONSTANT;
	}

	public void MoveSideward(float delta) {
		pos[0] += delta * uAxis[0] * DELTA_CONSTANT;
		pos[1] += delta * uAxis[1] * DELTA_CONSTANT;
		pos[2] += delta * uAxis[2] * DELTA_CONSTANT;
	}

	public void MoveUpward(float delta) {
		pos[0] += delta * vAxis[0] * DELTA_CONSTANT;
		pos[1] += delta * vAxis[1] * DELTA_CONSTANT;
		pos[2] += delta * vAxis[2] * DELTA_CONSTANT;
	}

	public float[] GetPosition() {
		return pos;
	}

	public float[] GetViewMatrix() {
		float[] matrix = new float[16];
		matrix[0] = uAxis[0];
		matrix[4] = uAxis[1];
		matrix[8] = uAxis[2];
		matrix[12] = -(pos[0] * uAxis[0] + pos[1] * uAxis[1] + pos[2]
				* uAxis[2]);
		matrix[1] = vAxis[0];
		matrix[5] = vAxis[1];
		matrix[9] = vAxis[2];
		matrix[13] = -(pos[0] * vAxis[0] + pos[1] * vAxis[1] + pos[2]
				* vAxis[2]);
		matrix[2] = nAxis[0];
		matrix[6] = nAxis[1];
		matrix[10] = nAxis[2];
		matrix[14] = -(pos[0] * nAxis[0] + pos[1] * nAxis[1] + pos[2]
				* nAxis[2]);
		matrix[3] = 0.0f;
		matrix[7] = 0.0f;
		matrix[11] = 0.0f;
		matrix[15] = 1.0f;
		return matrix;
	}

	public float getFovy() {
		return fovy * TO_RADIAN;
	}

	public void Zoom(float delta) {
		fovy = (fovy > fovy_MAX) ? fovy_MAX : fovy;
		fovy = (fovy < fovy_MIN) ? fovy_MIN : fovy;
		fovy += delta * DELTA_CONSTANT;
	}

	public void nomalize(float[] vec) {
		float amp = FloatMath.sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2]
				* vec[2]);
		vec[0] /= amp;
		vec[1] /= amp;
		vec[2] /= amp;
	}

}
