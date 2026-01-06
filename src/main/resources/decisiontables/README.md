# Decision Tables - Reglas Dinámicas desde CSV

Este directorio contiene **Decision Tables** (Tablas de Decisión) que demuestran el uso de **reglas dinámicas** en Drools.

## Concepto: Reglas Estáticas vs Dinámicas

### Reglas Estáticas (DRL)
- Definidas directamente en archivos `.drl`
- Requieren recompilación del código para cambios
- Ubicación: `src/main/resources/rules/*.drl`
- Ejemplos: `low-complexity-rules.drl`, `medium-complexity-rules.drl`, `high-complexity-rules.drl`

### Reglas Dinámicas (CSV → DRL)
- Definidas en archivos **CSV** (fuente de verdad)
- Pueden ser modificadas sin recompilar el código (en producción con recarga dinámica)
- Se compilan a DRL para ejecución
- Ubicación: `src/main/resources/decisiontables/*.csv` y `*.drl`

## Archivos CSV (Fuente de Reglas Dinámicas)

Los archivos CSV son la **fuente de verdad** para reglas dinámicas:

1. **transaction-rules.csv**: Reglas basadas en el monto de la transacción
2. **country-risk-rules.csv**: Reglas de evaluación de riesgo por país  
3. **account-tier-rules.csv**: Reglas basadas en el tier de cuenta y estado VIP

## Archivos DRL (Compilados)

Los archivos DRL son la versión compilada de los CSV:

1. **transaction-rules.drl**: Compilado desde `transaction-rules.csv`
2. **country-risk-rules.drl**: Compilado desde `country-risk-rules.csv`
3. **account-tier-rules.drl**: Compilado desde `account-tier-rules.csv`

## Flujo de Trabajo

```
CSV (Fuente) → Compilación → DRL (Ejecutable) → Drools Engine
```

1. **Desarrollo**: Modificar los archivos CSV según necesidades de negocio
2. **Compilación**: Los CSV se compilan a DRL (manual en esta POC, automático en producción)
3. **Ejecución**: Drools ejecuta las reglas desde los DRL compilados

## Ventajas de Reglas Dinámicas (CSV)

- ✅ **Fácil modificación**: Usuarios de negocio pueden editar CSV sin conocer DRL
- ✅ **Sin recompilación**: En producción con recarga dinámica, cambios en CSV se aplican sin reiniciar
- ✅ **Formato tabular**: Más intuitivo para reglas con múltiples condiciones similares
- ✅ **Versionado**: Los CSV pueden versionarse y rastrearse independientemente

## Uso en la POC

Las Decision Tables se cargan automáticamente al iniciar la aplicación y están disponibles a través de:

- **Endpoint**: `/api/decision-tables/evaluate`
- **Controlador**: `DecisionTableController`
- **Servicio**: `DecisionTableService`

## Próximos Pasos (Producción)

Para producción, se recomienda:

1. **Compilación automática**: Usar `SpreadsheetCompiler` para compilar CSV a DRL en tiempo de ejecución
2. **Recarga dinámica**: Implementar un mecanismo para recargar CSV modificados sin reiniciar
3. **Validación**: Validar formato CSV antes de compilar
4. **Monitoreo**: Logging de cambios en reglas dinámicas

## Referencias

- [Drools Decision Tables Documentation](https://docs.drools.org/latest/drools-docs/drools/decision-tables/index.html)
- [Drools User Guide](https://docs.drools.org/latest/drools-docs/drools/introduction/index.html)
