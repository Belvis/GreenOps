export function EcoGradeBadge({ grade }: { grade: 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'G' }) {
  const colors: Record<string, string> = {
    A: 'bg-emerald-100 text-emerald-800 border-emerald-300',
    B: 'bg-green-100 text-green-800 border-green-300',
    C: 'bg-lime-100 text-lime-800 border-lime-300',
    D: 'bg-yellow-100 text-yellow-800 border-yellow-300',
    E: 'bg-amber-100 text-amber-800 border-amber-300',
    F: 'bg-orange-100 text-orange-800 border-orange-300',
    G: 'bg-rose-100 text-rose-800 border-rose-300',
  };
  return (
    <span className={'inline-flex items-center justify-center w-8 h-8 rounded-lg font-bold text-sm border ' + colors[grade]}>
      {grade}
    </span>
  );
}

export function StatusBadge({ type, label }: { type: 'gdpr' | 'green' | 'warning' | 'role'; label: string }) {
  const styles = {
    gdpr: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/50 dark:text-blue-300 dark:border-blue-800',
    green: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/50 dark:text-emerald-300 dark:border-emerald-800',
    warning: 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/50 dark:text-amber-300 dark:border-amber-800',
    role: 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700',
  };
  return (
    <span className={'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ' + styles[type]}>
      {label}
    </span>
  );
}
