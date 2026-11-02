


export function Icon({ name = 'grid', size = 20 }) {
  const paths = {
    settings: 'M4 7h16 M4 17h16 M8 4v6 M16 14v6',
    chat: 'M21 11a8 8 0 0 1-8 8H7l-5 3 1.5-6A8 8 0 1 1 21 11 M7 10h10 M7 14h6',
    grid: 'M3 3h7v7H3z M14 3h7v7h-7z M3 14h7v7H3z M14 14h7v7h-7z',
    goal: 'M12 3a9 9 0 1 0 9 9 M12 7a5 5 0 1 0 5 5 M12 12l9-9 M16 3h5v5',
    briefcase: 'M3 7h18v13H3z M8 7V3h8v4 M3 12c6 4 12 4 18 0 M12 12v4',
    chart: 'M4 20V10 M12 20V4 M20 20v-7',
    book: 'M12 5C8 2 4 3 2 4v15c4-2 7-1 10 1 3-2 6-3 10-1V4c-4-2-7-1-10 1v15',
    arrow: 'M4 12h16 M14 6l6 6-6 6',
    exit: 'M9 4H3v16h6 M10 12h11 M16 7l5 5-5 5',
    plus: 'M12 5v14 M5 12h14',
    upload: 'M12 16V3 M7 8l5-5 5 5 M4 16v5h16v-5',
    check: 'M5 12l4 4L19 6',
    search: 'M21 21l-5-5 M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0',
  }
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={paths[name] || paths.grid}/></svg>
}

export function PageHeading({ eyebrow, title, description, children }) {
  return <div className="page-heading"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{description}</p></div>{children}</div>
}
export function ErrorNotice({ message, retry }) {
  return message ? <div className="notice error" role="alert"><span>{message}</span>{retry && <button className="text-button" onClick={retry}>Try again</button>}</div> : null
}
export function Empty({ icon, title, description, children }) {
  return <div className="empty"><span className="icon-tile"><Icon name={icon} size={26}/></span><h3>{title}</h3><p>{description}</p>{children}</div>
}
export function Loading() { return <div className="loading" role="status"><span className="spinner"/>Loading your workspace…</div> }

