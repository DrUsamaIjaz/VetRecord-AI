import { useState, useEffect } from "react";
import { supabase } from "./lib/supabase";
import Login from "./components/Login";
import Home from "./components/Home";
import { Loader2 } from "lucide-react";

export default function App() {
  const [session, setSession] = useState<boolean | null>(null);

  useEffect(() => {
    // Check for existing session on mount
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(!!session);
    });

    // Listen for auth state changes (login/logout)
    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(!!session);
    });

    return () => subscription.unsubscribe();
  }, []);

  // Loading: show spinner while checking session
  if (session === null) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-primary-light">
        <Loader2 size={32} className="animate-spin text-primary" />
      </div>
    );
  }

  // Not logged in: show login
  if (!session) {
    return <Login />;
  }

  // Logged in: show home
  return <Home />;
}
