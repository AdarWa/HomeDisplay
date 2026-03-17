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

export const fetchDevices = () => request('');

export const pingDevice = (deviceId) => request(`/${deviceId}/ping`, { method: 'POST' });

export const syncDevice = (deviceId) => request(`/${deviceId}/sync`, { method: 'POST' });

export const rebootDevice = (deviceId) => request(`/${deviceId}/reboot`, { method: 'POST' });

export const updateDeviceConfig = (deviceId, config) =>
  request(`/${deviceId}/config`, {
    method: 'PUT',
    body: JSON.stringify({ config }),
  });

export const deleteDevice = (deviceId) => request(`/${deviceId}`, { method: 'DELETE' });
