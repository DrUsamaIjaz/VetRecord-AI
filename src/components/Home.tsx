import { useState, useEffect, useCallback } from "react";
import { supabase } from "../lib/supabase";
import RecordCard from "./RecordCard";
import AddRecord from "./AddRecord";
import { Plus, LogOut, Loader2, ClipboardList } from "lucide-react";

interface AnimalRecord {
  id: string;
  animal_type: string;
  animal_name: string;
  problem: string;
  record_date: string;
  created_at: string;
}

export default function Home() {
  const [records, setRecords] = useState<AnimalRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState("");

  const fetchRecords = useCallback(async () => {
    setError("");
    const { data, error: err } = await supabase
      .from("animal_records")
      .select("*")
      .order("created_at", { ascending: false });

    if (err) {
      setError("Couldn't load your records. Pull down to try again.");
    } else {
      setRecords(data || []);
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  const handleLogout = async () => {
    await supabase.auth.signOut();
  };

  return (
    <div className="min-h-screen bg-muted flex flex-col">
      {/* Header */}
      <header className="bg-primary text-white px-5 py-4 flex items-center justify-between shadow-md">
        <div className="flex items-center gap-2">
          <div className="w-9 h-9 bg-white/20 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-lg">V</span>
          </div>
          <h1 className="text-lg font-bold">VetRecord AI</h1>
        </div>
        <button
          onClick={handleLogout}
          className="p-2 hover:bg-white/15 rounded-lg transition cursor-pointer flex items-center gap-1 text-sm"
          aria-label="Log out"
        >
          <LogOut size={18} />
          <span className="hidden sm:inline">Logout</span>
        </button>
      </header>

      {/* Main content */}
      <main className="flex-1 px-4 py-5 max-w-lg mx-auto w-full">
        {/* Add Button */}
        <button
          onClick={() => setShowForm(true)}
          className="w-full flex items-center justify-center gap-2 bg-primary text-white text-lg font-bold py-4 rounded-2xl shadow-lg hover:bg-primary-hover active:scale-[0.97] transition-all duration-150 mb-6 cursor-pointer"
        >
          <Plus size={24} />
          Add New Record
        </button>

        {/* Records list */}
        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 text-foreground/50">
            <Loader2 size={32} className="animate-spin mb-3" />
            <span>Loading your records...</span>
          </div>
        ) : error ? (
          <div className="text-center py-16 px-4">
            <p className="text-foreground/60 mb-3">{error}</p>
            <button
              onClick={fetchRecords}
              className="text-accent font-semibold hover:underline cursor-pointer"
            >
              Try again
            </button>
          </div>
        ) : records.length === 0 ? (
          <div className="text-center py-16 px-6">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-light rounded-2xl mb-4">
              <ClipboardList size={28} className="text-primary" />
            </div>
            <h2 className="text-lg font-semibold text-foreground mb-1">
              No records yet
            </h2>
            <p className="text-sm text-foreground/60 max-w-xs mx-auto">
              Tap the button above to add your first animal health record. It'll
              show up right here.
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            <p className="text-xs text-foreground/50 font-medium px-1">
              {records.length} record{records.length !== 1 ? "s" : ""}
            </p>
            {records.map((record) => (
              <RecordCard key={record.id} record={record} />
            ))}
          </div>
        )}
      </main>

      {/* Add Record Modal */}
      {showForm && (
        <AddRecord
          onClose={() => setShowForm(false)}
          onSaved={fetchRecords}
        />
      )}
    </div>
  );
}
