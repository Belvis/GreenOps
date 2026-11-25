import AlertPreferencesPanel from './AlertPreferencesPanel';

export function NotificationsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Alertes email</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Recevez un email quand la performance écologique d'une ressource suivie se dégrade.
        </p>
      </div>
      <AlertPreferencesPanel />
    </div>
  );
}