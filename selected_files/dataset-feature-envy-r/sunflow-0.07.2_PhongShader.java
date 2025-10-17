package org.sunflow.core.shader;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Shader;
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;

public class PhongShader implements Shader {
    private Color diffuseColor = Color.GRAY;
    private Color specularColor = Color.GRAY;
    private float specularPower = 20;
    private int numRays = 4;

    private final RadianceCalculator radianceCalculator;
    private final PhotonScatterer photonScatterer;

    public PhongShader() {
        this.radianceCalculator = new RadianceCalculator();
        this.photonScatterer = new PhotonScatterer();
    }

    @Override
    public boolean update(ParameterList pl, SunflowAPI api) {
        ShaderConfigurator configurator = new ShaderConfigurator();
        configurator.updateParameters(pl, this);
        return true;
    }

    @Override
    public Color getRadiance(ShadingState state) {
        return radianceCalculator.calculateRadiance(state, diffuseColor, specularColor, specularPower, numRays);
    }

    @Override
    public void scatterPhoton(ShadingState state, Color power) {
        photonScatterer.scatter(state, power);
    }
}

public class RadianceCalculator {
    public Color calculateRadiance(ShadingState state, Color diffuseColor, Color specularColor, float specularPower, int numRays) {
        // Certifica-se de que estamos no lado correto do material
        state.faceforward();

        // Inicializa amostras de luz e caustic
        state.initLightSamples();
        state.initCausticSamples();

        // Calcula componentes difuso e especular
        return state.diffuse(diffuseColor).add(state.specularPhong(specularColor, specularPower, numRays));
    }
}

public class PhotonScatterer {
    public void scatter(ShadingState state, Color power) {
        // Certifica-se de que estamos no lado correto do material
        state.faceforward();

        // Dispersa fótons difusos
        state.storePhoton(state.getDiffusePhoton(power));
    }
}

public class ShaderConfigurator {
    public void updateParameters(ParameterList pl, PhongShader shader) {
        shader.setDiffuseColor(pl.getColor("diffuse", shader.getDiffuseColor()));
        shader.setSpecularColor(pl.getColor("specular", shader.getSpecularColor()));
        shader.setSpecularPower(pl.getFloat("power", shader.getSpecularPower()));
        shader.setNumRays(pl.getInt("samples", shader.getNumRays()));
    }
}