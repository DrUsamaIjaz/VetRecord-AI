import { useState } from "react";
import { supabase } from "../lib/supabase";
import { Phone, KeyRound, ArrowRight, Loader2 } from "lucide-react";

export default function Login() {
  const [phone, setPhone] = useState("");
  const [otp, setOtp] = useState("");
  const [step, setStep] = useState<"phone" | "otp">("phone");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const formatPhone = (raw: string) => {
    // Allow digits and leading +
    const cleaned = raw.replace(/[^\d+]/g, "");
    return cleaned;
  };

  const handleSendOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    const formatted = formatPhone(phone);
    if (formatted.length < 10) {
      setError("Please enter a valid phone number");
      return;
    }

    setLoading(true);
    const { error: err } = await supabase.auth.signInWithOtp({
      phone: formatted.startsWith("+") ? formatted : `+91${formatted}`,
    });
    setLoading(false);

    if (err) {
      setError(err.message);
    } else {
      setStep("otp");
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (otp.length < 6) {
      setError("Please enter the 6-digit code");
      return;
    }

    setLoading(true);
    const formatted = formatPhone(phone);
    const { error: err } = await supabase.auth.verifyOtp({
      phone: formatted.startsWith("+") ? formatted : `+91${formatted}`,
      token: otp,
      type: "sms",
    });
    setLoading(false);

    if (err) {
      setError("Incorrect code. Please try again.");
    }
  };

  const goBack = () => {
    setStep("phone");
    setOtp("");
    setError("");
  };

  return (
    <div className="min-h-screen bg-primary-light flex flex-col items-center justify-center px-6">
      <div className="w-full max-w-sm">
        {/* Logo / Title */}
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-primary rounded-2xl mb-4 shadow-lg">
            <span className="text-white text-2xl font-bold">V</span>
          </div>
          <h1 className="text-2xl font-bold text-foreground">VetRecord AI</h1>
          <p className="text-sm text-foreground/60 mt-1">
            Track your animals' health
          </p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-lg p-6">
          {step === "phone" ? (
            <form onSubmit={handleSendOtp} className="space-y-5">
              <div>
                <label
                  htmlFor="phone"
                  className="block text-sm font-semibold text-foreground mb-2"
                >
                  Phone Number
                </label>
                <div className="relative">
                  <Phone
                    size={18}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-foreground/40"
                  />
                  <input
                    id="phone"
                    type="tel"
                    inputMode="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="Enter your phone number"
                    className="w-full pl-10 pr-4 py-3.5 text-base bg-muted border border-border rounded-xl focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition"
                    autoFocus
                  />
                </div>
                <p className="text-xs text-foreground/50 mt-1.5">
                  We'll send you a one-time code via SMS
                </p>
              </div>

              {error && (
                <p className="text-sm text-destructive bg-red-50 px-3 py-2 rounded-lg">
                  {error}
                </p>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 bg-primary text-white text-base font-semibold py-3.5 rounded-xl hover:bg-primary-hover active:scale-[0.97] transition-all duration-150 disabled:opacity-60 cursor-pointer"
              >
                {loading ? (
                  <Loader2 size={20} className="animate-spin" />
                ) : (
                  <>
                    Send Code <ArrowRight size={18} />
                  </>
                )}
              </button>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp} className="space-y-5">
              <div>
                <label
                  htmlFor="otp"
                  className="block text-sm font-semibold text-foreground mb-2"
                >
                  Verification Code
                </label>
                <div className="relative">
                  <KeyRound
                    size={18}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-foreground/40"
                  />
                  <input
                    id="otp"
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    value={otp}
                    onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
                    placeholder="Enter 6-digit code"
                    className="w-full pl-10 pr-4 py-3.5 text-base tracking-[0.3em] text-center bg-muted border border-border rounded-xl focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition"
                    autoFocus
                  />
                </div>
                <p className="text-xs text-foreground/50 mt-1.5">
                  Code sent to {phone || "your phone"}
                </p>
              </div>

              {error && (
                <p className="text-sm text-destructive bg-red-50 px-3 py-2 rounded-lg">
                  {error}
                </p>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 bg-primary text-white text-base font-semibold py-3.5 rounded-xl hover:bg-primary-hover active:scale-[0.97] transition-all duration-150 disabled:opacity-60 cursor-pointer"
              >
                {loading ? (
                  <Loader2 size={20} className="animate-spin" />
                ) : (
                  "Verify & Sign In"
                )}
              </button>

              <button
                type="button"
                onClick={goBack}
                className="w-full text-sm text-accent hover:underline py-2 cursor-pointer"
              >
                ← Change phone number
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
