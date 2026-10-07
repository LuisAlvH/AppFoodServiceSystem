# Reglas de seguridad

## Archivos sensibles

No leas, abras, muestres ni analices:

- `.env`
- `credentials/**`
- archivos que contengan private keys
- contraseñas
- API keys
- tokens
- secretos

Para auditorías:

- Podés analizar `src/`.
- Podés analizar `pom.xml`.
- Podés analizar `application.yaml`.
- Podés analizar `.gitignore`.
- Podés revisar cómo se utilizan las variables de entorno.
- No necesitás conocer los valores reales de ningún secreto.

Nunca solicites ni reproduzcas valores secretos.
