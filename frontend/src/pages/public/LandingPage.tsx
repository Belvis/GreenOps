import { Link } from 'react-router-dom';
import { Globe, GitBranch, Cloud, ArrowRight, ShieldCheck, Sun, Moon } from 'lucide-react';
import { GreenOpsLogo } from '../../components/ui/GreenOpsLogo';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { useApp } from '../../context/AppContext';

export function LandingPage() {
  const { darkMode, toggleDarkMode } = useApp();

  return (
    <div className="min-h-screen flex flex-col bg-[#F8FAFC] dark:bg-slate-950 text-slate-900 dark:text-slate-100">
      <header className="border-b border-slate-200 dark:border-slate-800 bg-white/80 dark:bg-slate-900/80 backdrop-blur-xs sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <GreenOpsLogo />
          <div className="flex items-center gap-3">
            <button
              onClick={toggleDarkMode}
              className="p-2 rounded-lg text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
            >
              {darkMode ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <Link to="/login">
              <Button variant="ghost">Se connecter</Button>
            </Link>
            <Link to="/register">
              <Button variant="primary">Créer un compte</Button>
            </Link>
          </div>
        </div>
      </header>

      <main className="flex-1">
        <section className="max-w-5xl mx-auto px-6 pt-20 pb-16 text-center space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-400 text-xs font-semibold">
            <ShieldCheck size={14} /> Plateforme SaaS Green IT & Souveraineté RGPD
          </div>
          <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-slate-900 dark:text-white leading-tight">
            Le pilotage carbone de tout votre{' '}
            <span className="text-emerald-600 dark:text-emerald-400">cycle de vie logiciel</span>
          </h1>
          <p className="max-w-2xl mx-auto text-lg text-slate-600 dark:text-slate-400">
            Auditez l'éco-conception de vos sites web, chiffrez l'énergie consommée par vos pipelines CI/CD et choisissez l'hébergement Cloud le plus décarboné conforme au RGPD.
          </p>
          <div className="flex flex-wrap justify-center gap-4 pt-4">
            <Link to="/dashboard">
              <Button size="lg">
                Accéder à la plateforme <ArrowRight size={18} />
              </Button>
            </Link>
            <Link to="/login">
              <Button variant="secondary" size="lg">
                Se connecter
              </Button>
            </Link>
          </div>
        </section>

        <section className="max-w-7xl mx-auto px-6 py-12 grid md:grid-cols-3 gap-6">
          <Card className="p-6 space-y-4">
            <div className="w-11 h-11 rounded-lg bg-emerald-100 dark:bg-emerald-950 text-emerald-600 flex items-center justify-center">
              <Globe size={22} />
            </div>
            <h3 className="text-lg font-bold">Audit Web & EcoIndex</h3>
            <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
              Évaluation en temps réel via Google PageSpeed & Green Web Foundation. Note de A à G, empreinte eau/CO₂ et recommandations officielles Lighthouse.
            </p>
          </Card>

          <Card className="p-6 space-y-4">
            <div className="w-11 h-11 rounded-lg bg-emerald-100 dark:bg-emerald-950 text-emerald-600 flex items-center justify-center">
              <GitBranch size={22} />
            </div>
            <h3 className="text-lg font-bold">Usine Logicielle & CI/CD</h3>
            <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
              Analyse sans token de vos dépôts publics GitHub et GitLab. Conversion des durées de runners en kWh et détection de la dette écologique.
            </p>
          </Card>

          <Card className="p-6 space-y-4">
            <div className="w-11 h-11 rounded-lg bg-emerald-100 dark:bg-emerald-950 text-emerald-600 flex items-center justify-center">
              <Cloud size={22} />
            </div>
            <h3 className="text-lg font-bold">Simulateur Cloud & RGPD</h3>
            <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
              Modélisation vCPU/RAM/PUE croisée avec l'intensité électrique en direct (Electricity Maps) et filtrage souverain UE vs Cloud Act.
            </p>
          </Card>
        </section>
      </main>

      <footer className="border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 py-8 px-6 text-sm text-slate-500">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <GreenOpsLogo size="sm" />
            <span>• Licence MIT • Projet Master</span>
          </div>
          <div className="flex items-center gap-6">
            <span>Équipe : Belvis, Shiamamadanki, Marwa, Faizi</span>
            <a
              href="https://github.com/Belvis/GreenOps"
              target="_blank"
              rel="noreferrer"
              className="text-emerald-600 hover:underline font-medium"
            >
              Dépôt GitHub
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
