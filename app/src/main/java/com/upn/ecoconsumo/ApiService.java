package com.upn.ecoconsumo;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    // Consulta la lista de productos o sugerencias desde una API REST externa en JSON
    @GET("api/productos/sugerencias")
    Call<List<Producto>> obtenerSugerencias();
}
