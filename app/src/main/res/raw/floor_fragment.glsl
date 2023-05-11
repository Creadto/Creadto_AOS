precision mediump float;
uniform vec4 u_FloorColor;
uniform vec4 u_LineColor;
uniform float u_MaxDepth;
uniform float u_GridUnit;
varying vec3 v_Grid;

void main() {
    float depth = gl_FragCoord.z / gl_FragCoord.w; // Calculate world-space distance.

    if ((mod(abs(v_Grid.x), u_GridUnit) < 0.1) || (mod(abs(v_Grid.z), u_GridUnit) < 0.1)) {
        gl_FragColor = max(0.0, (u_MaxDepth - depth) / u_MaxDepth) * u_LineColor
        + min(1.0, depth / u_MaxDepth) * u_FloorColor;
    } else {
        gl_FragColor = u_FloorColor;
    }
}