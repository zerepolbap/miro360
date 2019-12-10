package com.bell_labs.drs.gearvr;

/**
 * Representation of Mesh data.
 */

public class MeshData {

    float[] vertices = new float[0];
    float[] normals = new float[0];
    float[] texcoords = new float[0];
    int[] triangles = new int[0];

    public boolean isEmpty() {
        return (vertices.length == 0) ||
                (normals.length == 0) ||
                (texcoords.length == 0) ||
                (triangles.length == 0);
    }
}
