"use client"

import { useEffect, useState } from "react"
import type { ReactNode } from "react"
import { Check, Loader2, RotateCcw, Settings2 } from "lucide-react"

import { Button } from "@/components/ui/button"
import { getMyPreferences, updateMyPreferences } from "@/lib/api/preferences/client"
import type { UserPreferences } from "@/lib/types/api/preferences"
import { getApiErrorMessage } from "@/lib/api/errors"

const initialMessage = "Changes are saved to your account when you update a setting."

type PreferenceKey = keyof UserPreferences

export function ProfileSettingsTab() {
  const [preferences, setPreferences] = useState<UserPreferences | null>(null)
  const [loading, setLoading] = useState(true)
  const [savingKey, setSavingKey] = useState<PreferenceKey | null>(null)
  const [notice, setNotice] = useState<{ type: "success" | "error"; text: string } | null>(null)

  useEffect(() => {
    let active = true
    getMyPreferences()
      .then((data) => { if (active) setPreferences(data) })
      .catch((error: unknown) => {
        if (active) setNotice({ type: "error", text: getApiErrorMessage(error, "Could not load your settings.") })
      })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  async function save<K extends PreferenceKey>(key: K, value: UserPreferences[K]) {
    if (savingKey || !preferences) return
    setSavingKey(key)
    setNotice(null)
    try {
      // Use a one-field partial update. Explicit false values must be preserved.
      const saved = await updateMyPreferences({ [key]: value } as Pick<UserPreferences, K>)
      setPreferences(saved)
      setNotice({ type: "success", text: "Setting saved." })
    } catch (error) {
      setNotice({ type: "error", text: getApiErrorMessage(error, "Could not save this setting.") })
    } finally {
      setSavingKey(null)
    }
  }

  if (loading) {
    return <div className="flex min-h-48 items-center justify-center gap-2 text-sm text-muted-foreground"><Loader2 className="h-4 w-4 animate-spin" />Loading your settings…</div>
  }

  if (!preferences) {
    return <div className="rounded-2xl border border-border/60 p-5"><p className="text-sm text-muted-foreground">Your settings could not be loaded.</p><Button variant="outline" className="mt-3" onClick={() => { setLoading(true); setNotice(null); getMyPreferences().then(setPreferences).catch((error: unknown) => setNotice({ type: "error", text: getApiErrorMessage(error) })).finally(() => setLoading(false)) }}><RotateCcw className="mr-2 h-4 w-4" />Try again</Button>{notice && <p role="alert" className="mt-3 text-sm text-destructive">{notice.text}</p>}</div>
  }

  const busy = Boolean(savingKey)

  return (
    <div className="space-y-5">
      <div className="flex items-start gap-3 rounded-2xl border border-border/60 bg-background/20 p-4">
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-primary/15 bg-primary/5 text-primary"><Settings2 className="h-5 w-5" /></span>
        <div><h3 className="font-semibold">Account preferences</h3><p className="mt-1 text-sm leading-5 text-muted-foreground">These options are loaded from your account and saved to the server when changed.</p></div>
      </div>

      {notice && <p role={notice.type === "error" ? "alert" : "status"} className={`rounded-xl border px-3 py-2 text-sm ${notice.type === "error" ? "border-destructive/25 bg-destructive/5 text-destructive" : "border-emerald-500/20 bg-emerald-500/5 text-emerald-300"}`}>{notice.text}</p>}

      <Section title="Privacy" description="Control what other marketplace users can see.">
        <SelectRow label="Profile visibility" value={preferences.profileVisibility} disabled={busy} options={[{ value: "PUBLIC", label: "Public profile" }, { value: "PRIVATE", label: "Private profile" }]} onChange={(value) => void save("profileVisibility", value as UserPreferences["profileVisibility"])} />
        <SelectRow label="Location visibility" value={preferences.locationVisibility} disabled={busy} options={[{ value: "EXACT", label: "Exact location" }, { value: "CITY", label: "City only" }, { value: "HIDDEN", label: "Hidden" }]} onChange={(value) => void save("locationVisibility", value as UserPreferences["locationVisibility"])} />
        <SelectRow label="Inventory visibility" value={preferences.inventoryVisibility} disabled={busy} options={[{ value: "PUBLIC", label: "Public inventory" }, { value: "PRIVATE", label: "Private inventory" }]} onChange={(value) => void save("inventoryVisibility", value as UserPreferences["inventoryVisibility"])} />
        <ToggleRow label="Show online status" description="Let other users see when you are active." checked={preferences.showOnlineStatus} disabled={busy} onChange={(checked) => void save("showOnlineStatus", checked)} />
        <ToggleRow label="Show unavailable listings" description="Include your unavailable items in public inventory." checked={preferences.showUnavailableListings} disabled={busy} onChange={(checked) => void save("showUnavailableListings", checked)} />
      </Section>

      <Section title="Trading preferences" description="Choose who can contact you and your minimum trust requirement.">
        <ToggleRow label="Accept trade requests" checked={preferences.acceptTradeRequests} disabled={busy} onChange={(checked) => void save("acceptTradeRequests", checked)} />
        <ToggleRow label="Accept buy offers" checked={preferences.acceptBuyOffers} disabled={busy} onChange={(checked) => void save("acceptBuyOffers", checked)} />
        <SelectRow label="Message permission" value={preferences.messagePermission} disabled={busy} options={[{ value: "EVERYONE", label: "Everyone" }, { value: "TRADERS_ONLY", label: "People I trade with" }, { value: "NOBODY", label: "Nobody" }]} onChange={(value) => void save("messagePermission", value as UserPreferences["messagePermission"])} />
        <div className="flex flex-col gap-3 py-3 sm:flex-row sm:items-center sm:justify-between">
          <div><p className="text-sm font-medium">Minimum trader trust score</p><p className="mt-1 text-xs text-muted-foreground">0 to 1000; applies to new offers.</p></div>
          <div className="flex items-center gap-3 sm:w-52"><input aria-label="Minimum trader trust score" type="range" min={0} max={1000} step={25} value={preferences.minimumTraderTrustScore} disabled={busy} onChange={(event) => setPreferences((current) => current ? { ...current, minimumTraderTrustScore: Number(event.target.value) } : current)} onPointerUp={(event) => void save("minimumTraderTrustScore", Number((event.currentTarget as HTMLInputElement).value))} onKeyUp={(event) => { if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", "Home", "End"].includes(event.key)) void save("minimumTraderTrustScore", Number((event.currentTarget as HTMLInputElement).value)) }} className="w-full accent-primary" /><span className="w-12 text-right text-sm tabular-nums">{preferences.minimumTraderTrustScore}</span></div>
        </div>
        <SelectRow label="Default listing type" value={preferences.defaultListingType} disabled={busy} options={[{ value: "SALE", label: "Sale" }, { value: "TRADE", label: "Trade" }, { value: "BOTH", label: "Sale and trade" }]} onChange={(value) => void save("defaultListingType", value as UserPreferences["defaultListingType"])} />
      </Section>

      <Section title="Notifications" description="Manage account notifications.">
        <ToggleRow label="Trade updates" checked={preferences.notifyTrades} disabled={busy} onChange={(checked) => void save("notifyTrades", checked)} />
        <ToggleRow label="Messages" checked={preferences.notifyMessages} disabled={busy} onChange={(checked) => void save("notifyMessages", checked)} />
        <ToggleRow label="System notifications" checked={preferences.notifySystem} disabled={busy} onChange={(checked) => void save("notifySystem", checked)} />
        <ToggleRow label="Login alerts" description="Email you when a login is detected." checked={preferences.loginAlerts} disabled={busy} onChange={(checked) => void save("loginAlerts", checked)} />
      </Section>

      {busy && <p className="flex items-center gap-2 text-xs text-muted-foreground"><Loader2 className="h-3.5 w-3.5 animate-spin" />Saving setting…</p>}
      {!busy && notice?.type === "success" && <p className="flex items-center gap-2 text-xs text-emerald-300"><Check className="h-3.5 w-3.5" />{initialMessage}</p>}
    </div>
  )
}

function Section({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  return <section className="rounded-2xl border border-border/60 bg-background/20 p-4 sm:p-5"><div className="mb-3"><h4 className="font-semibold">{title}</h4><p className="mt-1 text-xs leading-5 text-muted-foreground">{description}</p></div><div className="divide-y divide-border/50">{children}</div></section>
}

function ToggleRow({ label, description, checked, disabled, onChange }: { label: string; description?: string; checked: boolean; disabled: boolean; onChange: (checked: boolean) => void }) {
  return <label className="flex cursor-pointer items-center justify-between gap-4 py-3"><span><span className="block text-sm font-medium">{label}</span>{description && <span className="mt-1 block text-xs leading-5 text-muted-foreground">{description}</span>}</span><input type="checkbox" checked={checked} disabled={disabled} onChange={(event) => onChange(event.target.checked)} className="h-4 w-4 shrink-0 accent-primary disabled:cursor-not-allowed" /></label>
}

function SelectRow({ label, value, options, disabled, onChange }: { label: string; value: string; options: { value: string; label: string }[]; disabled: boolean; onChange: (value: string) => void }) {
  return <label className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between"><span className="text-sm font-medium">{label}</span><select value={value} disabled={disabled} onChange={(event) => onChange(event.target.value)} className="h-10 rounded-xl border border-border/70 bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-primary/25 disabled:opacity-60 sm:min-w-48">{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></label>
}
