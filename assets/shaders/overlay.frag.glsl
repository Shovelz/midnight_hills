#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_cloudTexture;
uniform sampler2D u_screenTexture;
uniform vec2 u_resolution;// screen size
void main() {
    //    vec4 clouds = texture2D(u_cloudTexture, v_texCoords);

    vec2 uv = v_texCoords;
    vec3 color = texture2D(u_screenTexture, uv).rgb;
    float alpha = texture2D(u_screenTexture, uv).a;

    vec4 mag = vec4(1, 1, 0, 0.3f);
    vec3 combined = mix(mag.rgb, color.rgb, mag.a);

    gl_FragColor = vec4(combined, 1) * v_color;
}
