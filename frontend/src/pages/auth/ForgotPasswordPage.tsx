import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, AlertCircle, CheckCircle2, ArrowLeft } from 'lucide-react';
import { GreenOpsLogo } from '../../components/ui/GreenOpsLogo';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { apiRequest } from '../../api/apiClient';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await apiRequest('/auth/forgot-password', {
        method: 'POST',
        skipAuth: true,
        body: JSON.stringify({ email }),
      });
      setSent(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Impossible d'envoyer l'email de réinitialisation.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-[#F8FAFC] dark:bg-slate-950 text-slate-900 dark:text-slate-100 p-6">
      <div className="max-w-6xl w-full mx-auto">
        <Link to="/">
          <GreenOpsLogo />
        </Link>
      </div>

      <div className="max-w-md w-full mx-auto my-auto">
        <Card className="p-8 space-y-6">
          <div className="text-center space-y-1">
            <h1 className="text-2xl font-bold">Mot de passe oublié</h1>
            <p className="text-sm text-slate-500 dark:text-slate-400">
              Recevez un lien sécurisé valable 15 minutes par email
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle size={16} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {sent ? (
            <div className="p-4 rounded-lg bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 text-xs space-y-2">
              <div className="flex items-center gap-2 font-semibold">
                <CheckCircle2 size={16} />
                <span>Email de réinitialisation envoyé</span>
              </div>
              <p>
                Si un compte correspond à l'adresse <strong>{email}</strong>, un lien à usage unique vient d'être transmis.
              </p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                  Adresse email du compte
                </label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="prenom.nom@exemple.fr"
                  className="w-full px-3.5 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <Button type="submit" disabled={loading} className="w-full">
                <Mail size={16} />
                {loading ? 'Envoi en cours...' : 'Envoyer le lien de réinitialisation'}
              </Button>
            </form>
          )}

          <div className="text-center">
            <Link to="/login" className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-emerald-600">
              <ArrowLeft size={14} /> Retour à la connexion
            </Link>
          </div>
        </Card>
      </div>

      <div className="text-center text-xs text-slate-400">
        GreenOps SaaS • Sécurité des accès
      </div>
    </div>
  );
}