import { useEffect, useState } from 'react'
import { api } from './api'

export function useResource(path) {
  const [state, setState] = useState({ data: [], loading: true, error: '' })
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    api(path, { signal: controller.signal }).then(data => {
      setState({ data, loading: false, error: '' })
    }).catch(error => {
      if (error.name !== 'AbortError') setState(previous => ({ ...previous, loading: false, error: error.message }))
    })
    return () => controller.abort()
  }, [path, revision])
  return { ...state, reload: () => { setState(previous => ({ ...previous, loading: true, error: '' })); setRevision(value => value + 1) }, setData: data => setState(previous => ({ ...previous, data: typeof data === 'function' ? data(previous.data) : data })) }
}


