import { useState } from "react";
import { supabase } from "../lib/supabase";
import {
  X,
  Loader2,
  ChevronDown,
  Calendar,
  FileText,
  Tag,
  AlertCircle,
} from "lucide-react";

interface Props {
  onClose: () => void;
  onSaved: () => void;
}

const ANIMAL_TYPES = ["Cow", "Buffalo", "Goat", "Sheep"] as const;

export default function AddRecord({ onClose, onSaved }: Props) {
  const [animalType, setAnimalType] = useState("");
  const [animalName, setAnimalName] = useState("");
  const [problem, setProblem] = useState("");
  const [recordDate, setRecordDate] = useState(
    new Date().toISOString().split("T")[0]
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (!animalType) {
      setError("Please select an animal type");
      return;
    }
    if (!animalName.trim()) {
      setError("Please enter the animal's name or number");
      return;
    }
    if (!problem.trim()) {
      setError("Please describe the problem");
      return;
    }

    setSaving(true);
    const { error: err } = await supabase.from("animal_records").insert({
      animal_type: animalType,
      animal_name: animalName.trim(),
      problem: problem.trim(),
      record_date: recordDate,
    });
    setSaving(false);

    if (err) {
      setError("We couldn't save that — try again?");
    } else {
      onSaved();
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/40 flex items-end sm:items-center justify-center p-0 sm:p-4">
      <div className="bg-white w-full sm:max-w-md sm:rounded-2xl rounded-t-2xl max-h-[90vh] overflow-y-auto shadow-xl animate-[slideUp_0.25s_ease-out]">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-border sticky top-0 bg-white rounded-t-2xl z-10">
          <h2 className="text-lg font-bold text-foreground">New Record</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-muted rounded-full transition cursor-pointer"
            aria-label="Close"
          >
            <X size={20} />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-5 space-y-5">
          {/* Animal Type */}
          <div>
            <label
              htmlFor="animalType"
              className="flex items-center gap-1.5 text-sm font-semibold text-foreground mb-2"
            >
              <Tag size={16} className="text-primary" />
              Animal Type
            </label>
            <div className="relative">
              <select
                id="animalType"
                value={animalType}
                onChange={(e) => setAnimalType(e.target.value)}
                className="w-full appearance-none pl-4 pr-10 py-3.5 text-base bg-muted border border-border rounded-xl focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition cursor-pointer"
              >
                <option value="" disabled>
                  Select type...
                </option>
                {ANIMAL_TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t === "Cow" && "🐄 "}
                    {t === "Buffalo" && "🐃 "}
                    {t === "Goat" && "🐐 "}
                    {t === "Sheep" && "🐑 "}
                    {t}
                  </option>
                ))}
              </select>
              <ChevronDown
                size={18}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-foreground/40 pointer-events-none"
              />
            </div>
          </div>

          {/* Animal Name */}
          <div>
            <label
              htmlFor="animalName"
              className="flex items-center gap-1.5 text-sm font-semibold text-foreground mb-2"
            >
              <FileText size={16} className="text-primary" />
              Animal Name / Number
            </label>
            <input
              id="animalName"
              type="text"
              value={animalName}
              onChange={(e) => setAnimalName(e.target.value)}
              placeholder="e.g. Lakshmi, Cow #3"
              className="w-full px-4 py-3.5 text-base bg-muted border border-border rounded-xl focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition"
            />
          </div>

          {/* Problem */}
          <div>
            <label
              htmlFor="problem"
              className="flex items-center gap-1.5 text-sm font-semibold text-foreground mb-2"
            >
              <AlertCircle size={16} className="text-primary" />
              What's the problem?
            </label>
            <textarea
              id="problem"
              rows={3}
              value={problem}
              onChange={(e) => setProblem(e.target.value)}
              placeholder="Describe what's wrong..."
              className="w-full px-4 py-3.5 text-base bg-muted border border-border rounded-xl focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition resize-none"
            />
          </div>

          {/* Date */}
          <div>
            <label
              htmlFor="recordDate"
              className="flex items-center gap-1.5 text-sm font-semibold text-foreground mb-2"
            >
              <Calendar size={16} className="text-primary" />
              Date
            </label>
            <input
              id="recordDate"
              type="date"
              value={recordDate}
              onChange={(e) => setRecordDate(e.target.value)}
              className="w-full px-4 py-3.5 text-base bg-muted border border-border rounded-xl focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition cursor-pointer"
            />
          </div>

          {/* Error */}
          {error && (
            <p className="text-sm text-destructive bg-red-50 px-3 py-2 rounded-lg">
              {error}
            </p>
          )}

          {/* Submit */}
          <button
            type="submit"
            disabled={saving}
            className="w-full flex items-center justify-center gap-2 bg-primary text-white text-base font-semibold py-3.5 rounded-xl hover:bg-primary-hover active:scale-[0.97] transition-all duration-150 disabled:opacity-60 cursor-pointer"
          >
            {saving ? (
              <Loader2 size={20} className="animate-spin" />
            ) : (
              "Save Record"
            )}
          </button>
        </form>
      </div>

      {/* Slide-up animation */}
      <style>{`
        @keyframes slideUp {
          from { transform: translateY(100%); }
          to { transform: translateY(0); }
        }
      `}</style>
    </div>
  );
}
