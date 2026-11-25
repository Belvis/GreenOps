import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  User,
  Star,
  Clock,
  Download,
  ExternalLink,
  Trash2,
  LogOut,
  Mail,
  Shield,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/Badges';
import { useApp, type UserRole } from '../../context/AppContext';
import { useUserActivity, type ActivityItem, type ActivityModule } from '../../context/UserActivityContext';

const ROLE_LABELS: Record<UserRole, string> = {
  ROLE_USER: 'Profil Découverte',
  ROLE_DEV: 'Développeur / DevOps',
  ROLE_INFRA: 'Ingénieur Infra / Cloud',
  ROLE_RSE: 'Décideur RSE',
  ROLE_ADMIN: 'Super-Admin SaaS',
};

const MODULE_LABELS: Record<ActivityModule, string> = {
  AUDIT_WEB: 'Audit Web & EcoIndex',
  GIT_CI: 'Analyse Git & CI/CD',
  CLOUD_SIM: 'Simulateur Cloud & RGPD',
};

type TabFilter = 'ALL' | 'RECENT' | 'FAVORITES' | 'DOWNLOADED';

export function ProfilePage() {
  const { userEmail, role, logout } = useApp();
  const {
    recentItems,
    favoriteItems,
    downloadedItems,
    toggleFavorite,
    downloadItem,
    removeItem,
  } = useUserActivity();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<TabFilter>('ALL');
  const [moduleFilter, setModuleFilter] = useState<'ALL' | ActivityModule>('ALL');

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const baseList: ActivityItem[] =
    activeTab === 'FAVORITES'
      ? favoriteItems
      : activeTab === 'DOWNLOADED'
      ? downloadedItems
      : recentItems;

  const filteredList =
    moduleFilter === 'ALL' ? baseList : baseList.filter((item) => item.module === moduleFilter);

  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300 mb-2">
            <User size={14} />
            Espace Personnel
          </div>
          <h1 className="text-2xl font-bold tracking-tight">Mon profil</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Retrouvez vos favoris, vos recherches récentes et vos recherches téléchargées sur l'ensemble des modules.
          </p>
        </div>

        <Button variant="secondary" onClick={handleLogout}>
          <LogOut size={15} />
          Se déconnecter
        </Button>
      </div>

      <Card className="p-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
            <Mail size={16} className="text-emerald-600" />
            <span className="font-medium">Adresse e-mail :</span>
            <span>{userEmail || 'Non renseignée'}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
            <Shield size={16} className="text-emerald-600" />
            <span className="font-medium">Habilitation :</span>
            <StatusBadge type="green" label={ROLE_LABELS[role]} />
          </div>
        </div>

        <div className="grid grid-cols-3 gap-3 text-center">
          <div className="px-4 py-2.5 rounded-lg bg-slate-50 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700">
            <div className="text-lg font-bold">{recentItems.length}</div>
            <div className="text-[11px] text-slate-500">Récentes</div>
          </div>
          <div className="px-4 py-2.5 rounded-lg bg-slate-50 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700">
            <div className="text-lg font-bold text-amber-600">{favoriteItems.length}</div>
            <div className="text-[11px] text-slate-500">Favoris</div>
          </div>
          <div className="px-4 py-2.5 rounded-lg bg-slate-50 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700">
            <div className="text-lg font-bold text-emerald-600">{downloadedItems.length}</div>
            <div className="text-[11px] text-slate-500">Téléchargées</div>
          </div>
        </div>
      </Card>

      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div className="flex flex-wrap gap-2">
          <Button
            size="sm"
            variant={activeTab === 'ALL' ? 'primary' : 'secondary'}
            onClick={() => setActiveTab('ALL')}
          >
            Toutes ({recentItems.length})
          </Button>
          <Button
            size="sm"
            variant={activeTab === 'RECENT' ? 'primary' : 'secondary'}
            onClick={() => setActiveTab('RECENT')}
          >
            <Clock size={14} /> Recherches récentes ({recentItems.length})
          </Button>
          <Button
            size="sm"
            variant={activeTab === 'FAVORITES' ? 'primary' : 'secondary'}
            onClick={() => setActiveTab('FAVORITES')}
          >
            <Star size={14} /> Favoris ({favoriteItems.length})
          </Button>
          <Button
            size="sm"
            variant={activeTab === 'DOWNLOADED' ? 'primary' : 'secondary'}
            onClick={() => setActiveTab('DOWNLOADED')}
          >
            <Download size={14} /> Recherches téléchargées ({downloadedItems.length})
          </Button>
        </div>

        <select
          value={moduleFilter}
          onChange={(e) => setModuleFilter(e.target.value as 'ALL' | ActivityModule)}
          className="text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2"
        >
          <option value="ALL">Tous les modules</option>
          <option value="AUDIT_WEB">Audit Web & EcoIndex</option>
          <option value="GIT_CI">Analyse Git & CI/CD</option>
          <option value="CLOUD_SIM">Simulateur Cloud & RGPD</option>
        </select>
      </div>

      <div className="space-y-3">
        {filteredList.length === 0 ? (
          <Card className="p-8 text-center text-sm text-slate-500 dark:text-slate-400">
            Aucun élément enregistré dans cette section pour le moment. Lancez un audit ou une analyse dans un module puis cliquez sur <strong>Favori</strong> ou <strong>Télécharger</strong>.
          </Card>
        ) : (
          filteredList.map((item) => (
            <Card key={item.id} className="p-5 flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
              <div className="space-y-1.5 min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <StatusBadge type="green" label={MODULE_LABELS[item.module]} />
                  {item.isFavorite && <StatusBadge type="warning" label="Favori" />}
                  {item.isDownloaded && <StatusBadge type="gdpr" label="Téléchargé" />}
                  <span className="text-xs text-slate-400">
                    {new Date(item.createdAt).toLocaleString('fr-FR')}
                  </span>
                </div>
                <div className="font-bold text-base truncate">{item.title}</div>
                <div className="text-xs text-slate-500 dark:text-slate-400">{item.subtitle}</div>
              </div>

              <div className="flex flex-wrap items-center gap-2 shrink-0">
                <Button
                  size="sm"
                  variant={item.isFavorite ? 'primary' : 'secondary'}
                  onClick={() => toggleFavorite(item.id)}
                >
                  <Star size={14} />
                  {item.isFavorite ? 'Favori' : 'Ajouter aux favoris'}
                </Button>

                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => downloadItem(item.id)}
                >
                  <Download size={14} />
                  Télécharger
                </Button>

                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => navigate(item.route)}
                >
                  <ExternalLink size={14} />
                  Ouvrir
                </Button>

                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => removeItem(item.id)}
                  title="Supprimer"
                >
                  <Trash2 size={14} />
                </Button>
              </div>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}