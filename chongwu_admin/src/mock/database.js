function clone(data) {
  return JSON.parse(JSON.stringify(data))
}

export function createSuccess(data, message = 'success') {
  return { code: 200, message, data }
}

export function createModuleStorage(key, seedData) {
  function read() {
    if (typeof window === 'undefined') {
      return clone(seedData)
    }
    const raw = window.localStorage.getItem(key)
    if (!raw) {
      write(seedData)
      return clone(seedData)
    }
    try {
      const parsed = JSON.parse(raw)
      return Array.isArray(parsed) ? parsed : clone(seedData)
    } catch {
      return clone(seedData)
    }
  }

  function write(data) {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(key, JSON.stringify(data))
    }
  }

  return {
    getAll() {
      return read()
    },
    setAll(data) {
      write(data)
      return clone(data)
    },
    reset() {
      write(seedData)
      return clone(seedData)
    },
  }
}

export function createPageResult(list, pageNum = 1, pageSize = 10) {
  const start = (pageNum - 1) * pageSize
  return {
    list: list.slice(start, start + pageSize),
    total: list.length,
    pageNum,
    pageSize,
  }
}

export function resolveMock(handler, delay = 180) {
  return new Promise((resolve, reject) => {
    window.setTimeout(() => {
      try {
        resolve(createSuccess(handler()))
      } catch (error) {
        reject(error)
      }
    }, delay)
  })
}
