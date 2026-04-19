#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_texture;// mask texture (your water shape)
uniform sampler2D u_screenTexture;// FBO texture (what Godot calls screen_texture)

uniform float u_time;
uniform float u_intensity;
uniform float u_clarity;
uniform float u_highlightScale;

uniform vec2 u_resolution;// screen size

// constants
const int MAX_RADIUS = 2;
const float HASHSCALE1 = 0.1031;
const vec3 HASHSCALE3 = vec3(0.1031, 0.1030, 0.0973);
const float RIPPLE_FREQ = 12.0;
const float RIPPLE_STRENGTH = 0.1;
const float HIGHLIGHT_POW = 2.1;
const float RIPPLE_SIZE = 2.0;

uniform sampler2D u_reflectionTex; // player reflection


mat2 rotate2D(float r) {
    return mat2(cos(r), sin(r), -sin(r), cos(r));
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * HASHSCALE1);
    p3 += dot(p3, p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * HASHSCALE3);
    p3 += dot(p3, p3.yzx + 19.19);
    return fract((p3.xx + p3.yz) * p3.zy);
}

void main() {
    vec2 uv = v_texCoords;
    vec3 color = texture2D(u_screenTexture, uv).rgb;
    // screen UV (0–1 across screen)
    vec2 screenUV = gl_FragCoord.xy / u_resolution;

    vec4 reflection = texture2D(u_reflectionTex, v_texCoords);

    // --- RIPPLE ---
    vec2 uv_scaled = uv * RIPPLE_SIZE;
    vec2 base_cell = floor(uv_scaled);
    vec2 ripple_offset = vec2(0.0);

    for (int j = -MAX_RADIUS; j <= MAX_RADIUS; ++j) {
        for (int i = -MAX_RADIUS; i <= MAX_RADIUS; ++i) {
            vec2 cell = base_cell + vec2(float(i), float(j));

            if (fract(hash12(cell) * 123.456) < u_intensity) {
                vec2 p = cell + hash22(cell);
                float t = fract(0.3 * u_time + hash12(cell));

                vec2 v = p - uv_scaled;
                v.y *= 1.5;

                float d = length(v) - (float(MAX_RADIUS) + 1.0) * t;

                float h = 0.001;
                float d1 = d - h;
                float d2 = d + h;

                float p1 = sin(RIPPLE_FREQ * d1) * smoothstep(-0.6, -0.3, d1) * smoothstep(0.0, -0.3, d1);
                float p2 = sin(RIPPLE_FREQ * d2) * smoothstep(-0.6, -0.3, d2) * smoothstep(0.0, -0.3, d2);

                ripple_offset += 0.5 * normalize(v) * ((p2 - p1) / (2.0 * h) * pow(1.0 - t, 2.0));
            }
        }
    }

    ripple_offset /= float((MAX_RADIUS * 2 + 1) * (MAX_RADIUS * 2 + 1));
    ripple_offset *= RIPPLE_STRENGTH;

    // --- DISTORTION ---
    vec2 wave_offset = vec2(
    sin(uv.x * 10.0 + u_time),
    cos(uv.y * 10.0 + u_time)
    ) * 0.005;

    vec2 distortion = ripple_offset * u_intensity + wave_offset;
    float edgeFade =
    smoothstep(0.0, 0.05, screenUV.x) *
    smoothstep(0.0, 0.05, screenUV.y) *
    smoothstep(1.0, 0.95, screenUV.x) *
    smoothstep(1.0, 0.95, screenUV.y);

    // --- SAMPLE SCREEN ---
    vec2 uv_distorted = screenUV + distortion * edgeFade;
    vec3 screen_color = texture2D(u_screenTexture, uv_distorted).rgb;

    // --- BASE WATER ---
    vec3 base_water_color = vec3(0, 0, 0);
    vec3 blended = mix(screen_color, base_water_color, u_clarity);

    // --- NORMAL + SPEC ---
    float eps = 0.0;

    vec2 dx = vec2(texture2D(u_screenTexture, screenUV).r - texture2D(u_screenTexture, screenUV).r);

    vec2 dy = vec2(texture2D(u_screenTexture, screenUV).r - texture2D(u_screenTexture, screenUV).r);

    vec3 normal = normalize(vec3(-dx.x, -dy.y, 1.0));
    vec3 light_dir = normalize(vec3(0.5, 0.5, 1.0));

    float spec = pow(max(dot(normal, light_dir), 0.0), 32.0) * 5.0;

    vec3 water_color = blended + spec;

    // --- HIGHLIGHTS ---
    vec2 wave_uv = (uv * u_highlightScale) + distortion;

    vec2 wave_n = vec2(0.0);
    vec2 wave_sum = vec2(0.0);

    float S = 10.0;
    mat2 rot = rotate2D(1.0);

    for (float j = 0.0; j < 30.0; ++j) {
        wave_uv *= rot;
        wave_n *= rot;

        vec2 q = wave_uv * S + j + wave_n + u_time;

        wave_n += sin(q);
        wave_sum += cos(q) / S;

        S *= 1.2;
    }

    float wave_len = max(length(wave_sum), 0.001);

    vec3 wave_highlight = vec3(1.0) *
    pow((wave_sum.x + wave_sum.y + 0.4) + 0.005 / wave_len, HIGHLIGHT_POW);

    float brightness = dot(wave_highlight, vec3(0.299, 0.587, 0.114));
    float blend_factor = smoothstep(1.3, 1.301, brightness);

    vec3 final_color = mix(water_color, wave_highlight, blend_factor);

    float alpha = texture2D(u_texture, uv).a;

    vec4 water = vec4(final_color, alpha);

    vec4 refl = texture2D(u_reflectionTex, uv_distorted);

    refl.a *= alpha;

    refl.rgb *= 0.6;
    refl.a *= 0.8;

    vec3 combined = mix(water.rgb, refl.rgb, refl.a);

    gl_FragColor = vec4(combined, water.a) * v_color;
}
