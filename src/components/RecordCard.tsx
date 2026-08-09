import { Calendar } from "lucide-react";

interface AnimalRecord {
  id: string;
  animal_type: string;
  animal_name: string;
  problem: string;
  record_date: string;
}

const animalIcons: Record<string, string> = {
  Cow: "🐄",
  Buffalo: "🐃",
  Goat: "🐐",
  Sheep: "🐑",
};

export default function RecordCard({ record }: { record: AnimalRecord }) {
  const date = new Date(record.record_date);
  const formattedDate = date.toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });

  return (
    <div className="bg-white rounded-xl border border-border p-4 shadow-sm hover:shadow-md transition-shadow duration-200">
      <div className="flex items-start gap-3">
        {/* Animal Icon */}
        <div className="flex-shrink-0 w-12 h-12 bg-primary-light rounded-xl flex items-center justify-center text-2xl">
          {animalIcons[record.animal_type] || "🐾"}
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <h3 className="font-semibold text-foreground text-base truncate">
              {record.animal_name}
            </h3>
            <span className="text-xs px-2 py-0.5 bg-accent-light text-accent rounded-full font-medium flex-shrink-0">
              {record.animal_type}
            </span>
          </div>

          <p className="text-sm text-foreground/70 line-clamp-2 mb-2">
            {record.problem}
          </p>

          <div className="flex items-center gap-1 text-xs text-foreground/50">
            <Calendar size={12} />
            <span>{formattedDate}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
