import { createClient } from "@supabase/supabase-js";

const supabaseUrl = "https://waqteuopxdowoptnzgjr.supabase.co";
const supabaseAnonKey =
  "sb_publishable_rDl21lvqywBM6mhcCK6c4g_g3ewLOvE";

export const supabase = createClient(supabaseUrl, supabaseAnonKey);
