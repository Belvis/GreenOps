import { Link } from 'react-router-dom';
import { Globe, GitBranch, Cloud, Lock, ArrowUpRight } from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { EcoGradeBadge, StatusBadge } from '../../components/ui/Badges';
import { useApp } from '../../context/AppContext';

export function DashboardPage() {
  const { role } = useApp();
  const isDev = ['ROLE_DEV', 'ROLE_ADMIN'].includes(role);
  const isInfra = ['ROLE_INFRA', 'ROLE_RSE', 'ROLE_ADMIN'].includes(role);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Tableau de bord GreenOps</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          Vue d'ensemble de votre empreinte environnementale logicielle et accès aux modules selon vos habilitations.
        </p>
      </div>

      {/* CARTES KPI DE SYNTHÈSE */}
      <div className="grid sm:grid-cols-3 gap-6">
        <Card className="p-6 flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-slate-500 uppercase">Dernier Audit Web</p>
            <p className="text-2xl font-bold mt-1">78 / 100</p>
            <p className="text-xs text-emerald-600 mt-1">1.44 gCO₂eq / visite</p>
          </div>
          <EcoGradeBadge grade="B" />
        </Card>

        <Card className="p-6">
          <p className="text-xs font-medium text-slate-500 uppercase">Énergie CI/CD Cumulée</p>
          <p className="text-2xl font-bold mt-1">4.28 kWh</p>
          <p className="text-xs text-slate-500 mt-1">Sur 12 workflows analysés</p>
        </Card>

        <Card className="p-6">
          <p className="text-xs font-medium text-slate-500 uppercase">Région Cloud Optimale</p>
          <p className="text-2xl font-bold mt-1">France (FR)</p>
          <div className="mt-1">
            <StatusBadge type="gdpr" label="Conforme RGPD • 32 gCO₂/kWh" />
          </div>
        </Card>
      </div>

      {/* RACCOURCIS VERS LES MODULES (AVEC VERROUILLAGE VISUEL SELON RÔLE) */}
      <div>
        <h2 className="text-lg font-bold mb-4">Modules d'évaluation</h2>
        <div className="grid md:grid-cols-3 gap-6">
          <Link to="/audit-web">
            <Card className="p-6 hover:border-emerald-500 transition-colors h-full flex flex-col justify-between">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <Globe className="text-emerald-600" size={22} />
                  <ArrowUpRight size={18} className="text-slate-400" />
                </div>
                <h3 className="font-bold">Module 1 : Audit Web</h3>
                <p className="text-xs text-slate-500">Analyser une URL publique (EcoIndex, Green Web, Lighthouse).</p>
              </div>
              <div className="pt-4">
                <StatusBadge type="green" label="Accessible" />
              </div>
            </Card>
          </Link>

          <Card className={isDev ? "p-6 h-full flex flex-col justify-between hover:border-emerald-500" : "p-6 h-full flex flex-col justify-between opacity-60"}>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <GitBranch className="text-emerald-600" size={22} />
                {isDev ? <ArrowUpRight size={18} className="text-slate-400" /> : <Lock size={16} />}
              </div>
              <h3 className="font-bold">Module 2 : Analyse Git & CI/CD</h3>
              <p className="text-xs text-slate-500">Chiffrer l'énergie des pipelines publics GitHub / GitLab.</p>
            </div>
            <div className="pt-4">
              {isDev ? (
                <Link to="/git-analyzer"><StatusBadge type="green" label="Ouvrir le module" /></Link>
              ) : (
                <StatusBadge type="warning" label="Rôle requis : Développeur" />
              )}
            </div>
          </Card>

          <Card className={isInfra ? "p-6 h-full flex flex-col justify-between hover:border-emerald-500" : "p-6 h-full flex flex-col justify-between opacity-60"}>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <Cloud className="text-emerald-600" size={22} />
                {isInfra ? <ArrowUpRight size={18} className="text-slate-400" /> : <Lock size={16} />}
              </div>
              <h3 className="font-bold">Module 3 : Simulateur Cloud</h3>
              <p className="text-xs text-slate-500">Arbitrage empreinte carbone (Electricity Maps) et RGPD.</p>
            </div>
            <div className="pt-4">
              {isInfra ? (
                <Link to="/cloud-simulator"><StatusBadge type="green" label="Ouvrir le module" /></Link>
              ) : (
                <StatusBadge type="warning" label="Rôle requis : Ingénieur Infra" />
              )}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
