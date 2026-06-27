async function runTests() {
  const baseUrl = 'http://localhost:3000/api';
  console.log('--- Iniciando Pruebas de API REST ---');
  
  try {
    // 1. Status check
    const statusRes = await fetch(`${baseUrl}/status`);
    const status = await statusRes.json();
    console.log('1. /status OK:', status);

    const testUser = `runner_${Date.now()}`;
    
    // 2. Registro
    console.log(`Registrando usuario: ${testUser}`);
    const regRes = await fetch(`${baseUrl}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: 'Corredor de Prueba',
        nombreUsuario: testUser,
        password: 'password123'
      })
    });
    console.log('2. /auth/register status:', regRes.status);

    // 3. Login
    const loginRes = await fetch(`${baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombreUsuario: testUser,
        password: 'password123'
      })
    });
    const loginData = await loginRes.json();
    console.log('3. /auth/login response:', loginData);
    const userId = loginData.idUsuario;

    // 4. Perfil de Usuario
    const userRes = await fetch(`${baseUrl}/usuarios/${userId}`);
    const userData = await userRes.json();
    console.log('4. /usuarios/:id response:', userData);

    // 5. Crear Meta
    console.log('Creando una meta de pasos de 1000...');
    const metaRes = await fetch(`${baseUrl}/metas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        idUsuario: userId,
        tipoMeta: 'PASOS',
        valorObjetivo: 1000.0
      })
    });
    const metaData = await metaRes.json();
    console.log('5. /metas response:', metaData);

    // 6. Consultar metas
    const getMetasRes = await fetch(`${baseUrl}/metas/usuario/${userId}`);
    const getMetasData = await getMetasRes.json();
    console.log('6. /metas/usuario/:id response:', getMetasData);

    // 7. Iniciar Entrenamiento
    console.log('Iniciando entrenamiento...');
    const startEntRes = await fetch(`${baseUrl}/entrenamientos/iniciar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        idUsuario: userId
      })
    });
    const startEntData = await startEntRes.json();
    console.log('7. /entrenamientos/iniciar response:', startEntData);
    const entId = startEntData.idEntrenamiento;

    // 8. Finalizar Entrenamiento (Completando la meta)
    console.log('Finalizando entrenamiento (enviando 1200 pasos)...');
    const endEntRes = await fetch(`${baseUrl}/entrenamientos/finalizar`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        idEntrenamiento: entId,
        pasos: 1200,
        calorias: 150,
        distancia: 1.2,
        tiempo: 600, // 10 minutos
        coordenadas: [
          { longitud: -101.123, latitud: 21.456 },
          { longitud: -101.124, latitud: 21.457 }
        ],
        puntoInicio: { longitud: -101.123, latitud: 21.456 },
        puntoFin: { longitud: -101.124, latitud: 21.457 }
      })
    });
    const endEntData = await endEntRes.json();
    console.log('8. /entrenamientos/finalizar response:', endEntData);

    // 9. Verificar metas actualizadas
    const checkMetasRes = await fetch(`${baseUrl}/metas/usuario/${userId}`);
    const checkMetasData = await checkMetasRes.json();
    console.log('9. Metas después de finalizar:', checkMetasData);

    // 10. Consultar Notificaciones (Debe haber una de meta completada)
    const notifRes = await fetch(`${baseUrl}/notificaciones/usuario/${userId}`);
    const notifData = await notifRes.json();
    console.log('10. /notificaciones/usuario/:id response:', notifData);

    // 11. Consultar Dashboard Semanal
    const weeklyRes = await fetch(`${baseUrl}/entrenamientos/semana/${userId}`);
    const weeklyData = await weeklyRes.json();
    console.log('11. /entrenamientos/semana/:id response:', weeklyData);

  } catch (error) {
    console.error('Error durante la ejecución del test:', error);
  }
}

runTests();
