const baseUrl = '/api/devices';

const request = async (path, options = {}) => {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || 'Request failed');
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
};

export const fetchDevices = () => request('/fetch');

export const pingDevice = (deviceId) => request(`/ping`, { method: 'POST', body: JSON.stringify({ id: deviceId}) });

export const syncDevice = (deviceId) => request(`/sync`, { method: 'POST', body: JSON.stringify({ id: deviceId}) });

export const rebootDevice = (deviceId) => request(`/reboot`, { method: 'POST', body: JSON.stringify({ id: deviceId}) });

export const updateDeviceConfig = (deviceId, config) =>
  request(`/updateConfig`, {
    method: 'PUT',
    body: JSON.stringify({ config: config, id: deviceId }),
  });

export const deleteDevice = (deviceId) => request(`/delete`, { method: 'DELETE', body: JSON.stringify({id: deviceId}) });
