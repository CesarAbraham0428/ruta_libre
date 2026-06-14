Nombre de la aplicación: Ruta Libre
Descripción general:
Ruta Libre es una aplicación para el monitoreo de actividad física enfocada en running, compuesta por tres versiones (smartwatch, móvil y Smart TV) que comparten información en tiempo real.
Smartwatch (Wear OS):

Funciones:

Inicio y finalización de sesión de actividad.
Visualización rápida de métricas durante la actividad mediante iconos y valores numéricos:
Distancia recorrida.
Cantidad de pasos.
Calorías estimadas.
Notificaciones (Logros) al momento de que un usuario llegue a completar una meta le aparecerá en el smartwatch

Dispositivo móvil (Android):
Funciones:
Visualización de la ruta recorrida en tiempo real.
Visualización de resultados diarios una vez terminada la sesión:
Distancia.
Pasos.
Calorías.
Tiempo de actividad.
Definición de metas diarias personalizadas.
Notificaciones (Logros) al momento de que un usuario llegue a completar una meta le aparecerá en la pantalla.
Creación o unión en grupos donde podrás visualizar tus resultados y compararlos con respecto a las demás personas.

Smart TV (Android TV):
Plataforma de análisis y visualización de rendimiento a nivel histórico.
Funciones:
Dashboard de métricas semanales
Comparación de rendimiento entre días
Análisis de tendencias:
Mejora o disminución del rendimiento respecto a la semana anterior.
Creación o unión en grupos donde podrás compartir tus resultados con las demás personas.
Visualización de métricas semanales de todas las personas que estén unidas al grupo
Visualización multimedia:
Podrás visualizar videos de ejercicios que te parezcan adecuados, además que se podrán guardar en la pestaña de “mis favoritos” los que más te gusten.

Requerimientos Funcionales de Ruta Libre.


Entidades del diagrama: Usuario (id_usuario, nombre, password, nombre_usuario), Grupo (id_grupo, nombre, codigo, descripcion), Ruta (id_ruta, coordenadas jsonb), Entrenamiento (id_entrenamiento, pasos, calorias, distancia, fecha_inicio, tiempo, punto_inicio, punto_fin) y Metas (id_metas, tipo_meta ENUM, valor_objetivo, valor_actual, terminada).

Relaciones: Usuario 1:N Entrenamiento, Usuario 1:N Metas, Usuario M:N Grupo, Ruta 1:1 Entrenamiento.


RF-01 Gestión de actividad física (Entrenamiento)

RF-01.1 El sistema deberá permitir al usuario iniciar una sesión de running desde el smartwatch, creando un nuevo registro de entrenamiento.
RF-01.2 El sistema deberá permitir al usuario finalizar una sesión de running desde el smartwatch, cerrando el registro de entrenamiento activo.
RF-01.3 El sistema deberá registrar la fecha y hora de inicio de cada actividad en el campo fecha_inicio del entrenamiento.
RF-01.4 El sistema deberá registrar la duración total de la actividad (campo tiempo, en segundos) al finalizar el entrenamiento.


RF-02 Monitoreo en tiempo real (Smartwatch)


RF-02.1 El sistema deberá mostrar la distancia recorrida durante la actividad (campo distancia del entrenamiento).
RF-02.2 El sistema deberá mostrar la cantidad de pasos realizados durante la actividad (campo pasos del entrenamiento).
RF-02.3 El sistema deberá mostrar las calorías estimadas consumidas durante la actividad (campo calorias del entrenamiento).
RF-02.4 El sistema deberá actualizar en tiempo real los valores de distancia, pasos y calorias durante la sesión de actividad.
RF-02.5 El sistema deberá mostrar el tiempo transcurrido de la actividad en curso, calculado a partir de fecha_inicio.


RF-03 Seguimiento de ruta (Dispositivo móvil)


RF-03.1 El sistema deberá mostrar en un mapa la ruta recorrida por el usuario en tiempo real, utilizando el arreglo de coordenadas (coordenadas: [{longitud, latitud}]) de la entidad Ruta.
RF-03.2 El sistema deberá actualizar la ubicación del usuario durante la actividad, agregando nuevos puntos {longitud, latitud} al campo coordenadas de la Ruta.
RF-03.3 El sistema deberá almacenar la ruta recorrida al finalizar la actividad, creando un registro en la tabla Ruta vinculado de forma 1:1 al Entrenamiento, y registrar el punto_inicio y punto_fin (tipo geometry) en el entrenamiento.


RF-04 Resumen de actividad


RF-04.1 El sistema deberá generar un resumen de actividad al finalizar una sesión de running, a partir de los datos del registro de Entrenamiento.
RF-04.2 El sistema deberá mostrar en el dispositivo móvil la distancia total recorrida (campo distancia).
RF-04.3 El sistema deberá mostrar en el dispositivo móvil la cantidad total de pasos realizados (campo pasos).
RF-04.4 El sistema deberá mostrar en el dispositivo móvil las calorías consumidas durante la actividad (campo calorias).
RF-04.5 El sistema deberá mostrar en el dispositivo móvil el tiempo total de actividad (campo tiempo).
RF-04.6 El sistema deberá almacenar el historial de actividades realizadas por el usuario, manteniendo todos los registros de Entrenamiento asociados al id_usuario.


RF-05 Gestión de metas (Metas)


RF-05.1 El sistema deberá permitir definir metas diarias personalizadas desde el dispositivo móvil, generando un registro en la tabla Metas asociado al usuario.
RF-05.2 El usuario podrá establecer metas de distancia (tipo_meta = 'distancia').
RF-05.3 El usuario podrá establecer metas de pasos (tipo_meta = 'pasos').
RF-05.4 El usuario podrá establecer metas de calorías (tipo_meta = 'calorias').
RF-05.5 El usuario podrá establecer metas de tiempo de actividad (tipo_meta = 'tiempo').
RF-05.6 El sistema deberá monitorear el progreso de las metas establecidas, actualizando el campo valor_actual conforme se registra actividad y comparándolo contra valor_objetivo.


RF-06 Notificaciones y logros


RF-06.1 El sistema deberá detectar cuando una meta diaria haya sido alcanzada, es decir, cuando valor_actual >= valor_objetivo, y marcar el campo terminada de la meta como true.
RF-06.2 El sistema deberá mostrar una notificación de logro en el smartwatch cuando una meta cambie su estado terminada a true.
RF-06.3 El sistema deberá mostrar una notificación de logro en el dispositivo móvil cuando una meta cambie su estado terminada a true.
RF-06.4 El sistema deberá sincronizar las notificaciones de logros entre los dispositivos vinculados del usuario (smartwatch, móvil y Android TV).


RF-07 Sincronización entre dispositivos


RF-07.1 El sistema deberá permitir vincular un smartwatch Wear OS con un dispositivo móvil Android, asociando ambos dispositivos a la misma cuenta de Usuario.
RF-07.2 El sistema deberá sincronizar automáticamente la información de Entrenamiento y Metas entre el smartwatch y el dispositivo móvil.
RF-07.3 El sistema deberá enviar al dispositivo móvil el registro completo de Entrenamiento (incluyendo pasos, calorias, distancia, tiempo y la Ruta asociada) cuando finalice en el smartwatch.
RF-07.4 El sistema deberá sincronizar el historial de Entrenamientos y Metas del usuario con la aplicación de Android TV.
RF-07.5 El sistema deberá mantener consistencia de datos de Usuario, Entrenamiento, Ruta y Metas entre los dispositivos vinculados.


RF-08 Gestión de grupos (Grupo)


RF-08.1 El sistema deberá permitir crear grupos desde la aplicación móvil, generando un registro en Grupo con nombre, codigo (único) y descripcion.
RF-08.2 El sistema deberá permitir unirse a grupos existentes mediante el codigo del grupo, creando la relación M:N entre Usuario y Grupo.
RF-08.3 El sistema deberá mostrar los resultados individuales (registros de Entrenamiento) de los integrantes del grupo.
RF-08.4 El sistema deberá permitir comparar los resultados personales (distancia, pasos, calorias, tiempo) con los de otros miembros del grupo al que pertenece el usuario.
RF-08.5 El sistema deberá sincronizar la información de Grupo y de sus integrantes entre el móvil y Android TV.


RF-09 Dashboard semanal (Android TV)


RF-09.1 El sistema deberá mostrar la suma de distancia de los entrenamientos del usuario acumulada durante la semana.
RF-09.2 El sistema deberá mostrar la suma de pasos de los entrenamientos del usuario acumulados durante la semana.
RF-09.3 El sistema deberá mostrar la suma de calorias de los entrenamientos del usuario consumidas durante la semana.
RF-09.4 El sistema deberá mostrar la suma de tiempo de los entrenamientos del usuario acumulado durante la semana.
RF-09.5 El sistema deberá presentar gráficas de rendimiento diario (distancia, pasos, calorias, tiempo por día) dentro del periodo semanal.


RF-10 Comparación y análisis de rendimiento


RF-10.1 El sistema deberá comparar el rendimiento (suma de distancia, pasos, calorias y tiempo de los Entrenamientos) de una semana con respecto a la semana anterior.
RF-10.2 El sistema deberá calcular el porcentaje de mejora o disminución en distancia recorrida.
RF-10.3 El sistema deberá calcular el porcentaje de mejora o disminución en pasos realizados.
RF-10.4 El sistema deberá calcular el porcentaje de mejora o disminución en calorias consumidas.
RF-10.5 El sistema deberá calcular el porcentaje de mejora o disminución en tiempo de actividad.
RF-10.6 El sistema deberá mostrar tendencias de rendimiento histórico a partir de los registros de Entrenamiento del usuario.


RF-11 Estadísticas grupales (Android TV)


RF-11.1 El sistema deberá mostrar las métricas semanales (distancia, pasos, calorias, tiempo) de los Entrenamientos de cada integrante del grupo.
RF-11.2 El sistema deberá permitir comparar el rendimiento entre los miembros del grupo en base a las métricas anteriores.
RF-11.3 El sistema deberá mostrar rankings de rendimiento dentro del grupo, ordenando a los integrantes por alguna de las métricas (distancia, pasos, calorias, tiempo).