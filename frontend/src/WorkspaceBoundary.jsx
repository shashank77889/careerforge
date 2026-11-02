import { Component } from 'react'
export default class WorkspaceBoundary extends Component {
 state={failed:false}
 static getDerivedStateFromError(){return {failed:true}}
 componentDidCatch(){console.error('CareerForge workspace render failed. Reload the page; report the page URL if it continues.')}
 render(){
  return this.state.failed?<section className="panel" role="alert"><h2>This page couldn’t load</h2><p>Your saved account data is still on the server. Reload to try again.</p><button className="primary" onClick={()=>window.location.reload()}>Reload page</button></section>:this.props.children
 }
}
