/**
 * 这个仓不用 axios —— 直接用浏览器自带的 fetch，
 * 几百行的小系统没必要再引一个库。
 */
async function call(path, options = {}) {
  const res = await fetch('/api' + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })
  const text = await res.text()
  const data = text ? JSON.parse(text) : null
  if (!res.ok) {
    throw new Error((data && data.message) || ('请求失败：' + res.status))
  }
  return data
}

const build = (path) => ({
  list: (params = {}) => {
    const qs = new URLSearchParams(
      Object.entries(params).filter(([, v]) => v !== '' && v != null)
    ).toString()
    return call(path + (qs ? '?' + qs : ''))
  },
  add: (body) => call(path, { method: 'POST', body: JSON.stringify(body) }),
  save: (id, body) => call(`${path}/${id}`, { method: 'PUT', body: JSON.stringify(body) })
})

export const areaApi = build('/areas')
export const machineApi = build('/machines')
export const gymClassApi = build('/classes')
export const memberApi = build('/members')
export const lockerApi = build('/lockers')

export const visitApi = {
  ...build('/visits'),
  board: () => call('/visits/board'),
  returnKey: (id) => call(`/visits/${id}/key`, { method: 'PUT' }),
  leave: (id) => call(`/visits/${id}/leave`, { method: 'PUT' })
}

export default call
