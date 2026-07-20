const mqtt = require('mqtt');
const { randomUUID } = require('crypto');

let client = null;

function configuracionCompleta() {
  return Boolean(
    process.env.MQTT_HOST &&
    process.env.MQTT_USERNAME &&
    process.env.MQTT_PASSWORD
  );
}

function iniciarMqtt() {
  if (client || !configuracionCompleta()) {
    if (!configuracionCompleta()) {
      console.warn('MQTT desactivado: faltan variables MQTT_* en el archivo .env.');
    }
    return client;
  }

  const port = Number.parseInt(process.env.MQTT_PORT || '8883', 10);
  client = mqtt.connect(`mqtts://${process.env.MQTT_HOST}:${port}`, {
    username: process.env.MQTT_USERNAME,
    password: process.env.MQTT_PASSWORD,
    clientId: `ruta-libre-backend-${randomUUID()}`,
    clean: true,
    reconnectPeriod: 5_000,
    connectTimeout: 10_000,
    rejectUnauthorized: true
  });

  client.on('connect', () => {
    console.log('Conectado a HiveMQ Cloud correctamente.');
  });

  client.on('reconnect', () => {
    console.log('Reconectando con HiveMQ Cloud...');
  });

  client.on('error', (error) => {
    console.error('Error de conexión MQTT:', error.message);
  });

  return client;
}

function publicarEvento(topic, data) {
  const mqttClient = iniciarMqtt();
  if (!mqttClient) return;

  const payload = JSON.stringify({
    version: 1,
    eventId: randomUUID(),
    fechaEvento: new Date().toISOString(),
    data
  });

  mqttClient.publish(topic, payload, { qos: 1, retain: false }, (error) => {
    if (error) {
      console.error(`No se pudo publicar en ${topic}:`, error.message);
    } else {
      console.log(`Evento MQTT publicado: ${topic}`);
    }
  });
}

function estaConectado() {
  return Boolean(client?.connected);
}

module.exports = {
  iniciarMqtt,
  publicarEvento,
  estaConectado
};
