import { Link, useLocation } from 'react-router-dom'

export default function PlaceholderPage() {
  const location = useLocation()
  const title = location.pathname.slice(1).replaceAll('-', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase())
  return <main className="message-page"><span className="eyebrow">Coming up</span><h1>{title}</h1><p>This TableTap space is ready for a future update.</p><Link to=".." className="button button-primary">Back to dashboard</Link></main>
}
