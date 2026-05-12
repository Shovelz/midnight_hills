#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
varying vec2 v_worldPos;

uniform sampler2D u_screenTexture;
uniform sampler2D u_noiseTexture;

uniform float u_time;

// cloud params
uniform float u_noiseStrength;
uniform float u_shadowStrength;
uniform vec2 u_uvScale;
uniform vec2 u_speed;

// light direction (WORLD SPACE, normalized)
uniform vec3 u_lightDir;

//--------------------------------------------

void main() {
    vec4 screen = texture2D(u_screenTexture, v_texCoords);

    //----------------------------------------
    // reconstruct world position
    vec3 rayStart = vec3(v_worldPos.xy, 0.0);

    // directional light ray (pointing DOWN)
    vec3 rayDir = normalize(-u_lightDir);

    //----------------------------------------
    // plane: y = 0
    float t = -rayStart.y / rayDir.y;

    vec3 P = rayStart + rayDir * t;

    //----------------------------------------
    // project onto XZ plane
    vec2 uv = P.xz;

    // animate clouds
    vec2 uvOffset = u_time * u_speed;

    vec2 sampleUV = uv * u_uvScale + uvOffset;

    //----------------------------------------
    // sample noise
    float noise = texture2D(u_noiseTexture, sampleUV).r;

    float clouds = smoothstep(0.2, 1.0, 1.0 - noise * u_noiseStrength);

    // soften
    clouds = clouds * 0.9 + 0.1;

    //----------------------------------------
    // convert to shadow
    float shadow = mix(1.0, clouds, u_shadowStrength);

    vec3 finalColor = screen.rgb * shadow;

    gl_FragColor = vec4(finalColor, screen.a);
}
