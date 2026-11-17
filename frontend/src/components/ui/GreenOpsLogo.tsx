import { Leaf, Server } from 'lucide-react';

export function GreenOpsLogo({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const iconSize = size === 'sm' ? 18 : size === 'lg' ? 28 : 22;
  const textSize = size === 'sm' ? 'text-lg' : size === 'lg' ? 'text-2xl' : 'text-xl';

  return (
    <div className="inline-flex items-center gap-2.5 select-none">
      <div className="relative flex items-center justify-center w-9 h-9 rounded-xl bg-emerald-500 text-white shadow-sm shadow-emerald-500/20">
        <Server size={iconSize - 3} className="opacity-90" />
        <Leaf size={iconSize - 6} className="absolute -top-1 -right-1 text-emerald-200 fill-emerald-600" />
      </div>
      <span className={'font-extrabold tracking-tight text-slate-900 dark:text-white ' + textSize}>
        Green<span className="text-emerald-600 dark:text-emerald-400">Ops</span>
      </span>
    </div>
  );
}
