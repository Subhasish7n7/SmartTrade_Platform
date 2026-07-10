"use client"

import { useState } from "react"

import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"

import { Button } from "@/components/ui/button"

import { Badge } from "@/components/ui/badge"

import { createItem } from "@/lib/api/items/client"

import { ImageUploadGrid } from "./image-upload-grid"
import { ListingPreviewCard } from "./listing-preview-card"
import { LocationPicker } from "./location-picker"

export function ItemListingForm() {
  const [loading, setLoading] = useState(false)

  const [labelInput, setLabelInput] = useState("")

  const [form, setForm] = useState({
    itemName: "",

    userPrice: "",

    category: "",

    condition: "",

    description: "",

    labels: [] as string[],

    imageUrls: [] as string[],

    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined,

    city: "",
    state: "",
    locality: "",

    forTrade: true,
    forSale: false,
  })

  async function handleSubmit() {
    try {
      setLoading(true)

      await createItem({
        ...form,
        userPrice: Number(form.userPrice),
      })
    } finally {
      setLoading(false)
    }
  }

  function addTag() {
    if (!labelInput.trim()) return

    setForm((prev) => ({
      ...prev,
      labels: [...prev.labels, labelInput],
    }))

    setLabelInput("")
  }

  return (
    <div className="grid lg:grid-cols-12 gap-6">
      <div className="lg:col-span-8 space-y-6">
        <div className="content-surface p-5">
          <h2 className="font-semibold mb-4">Photos</h2>

          <ImageUploadGrid
            value={form.imageUrls}
            onChange={(urls) =>
              setForm((prev) => ({
                ...prev,
                imageUrls: urls,
              }))
            }
          />
        </div>

        <div className="content-surface p-5 space-y-4">
          <h2 className="font-semibold">Item Details</h2>

          <Input
            placeholder="Item Name"
            value={form.itemName}
            onChange={(e) =>
              setForm({
                ...form,
                itemName: e.target.value,
              })
            }
          />

          <Input
            placeholder="Category"
            value={form.category}
            onChange={(e) =>
              setForm({
                ...form,
                category: e.target.value,
              })
            }
          />

          <Input
            placeholder="Condition"
            value={form.condition}
            onChange={(e) =>
              setForm({
                ...form,
                condition: e.target.value,
              })
            }
          />
        </div>

        <div className="content-surface p-5 space-y-4">
          <h2 className="font-semibold">Pricing</h2>

          <Input
            type="number"
            placeholder="Expected Trade Value"
            value={form.userPrice}
            onChange={(e) =>
              setForm({
                ...form,
                userPrice: e.target.value,
              })
            }
          />
        </div>

        <div className="content-surface p-5">
          <h2 className="font-semibold mb-4">Description</h2>

          <Textarea
            rows={6}
            value={form.description}
            onChange={(e) =>
              setForm({
                ...form,
                description: e.target.value,
              })
            }
          />
        </div>

        <div className="content-surface p-5">
          <h2 className="font-semibold mb-4">Labels</h2>

          <div className="flex gap-2">
            <Input value={labelInput} onChange={(e) => setLabelInput(e.target.value)} />

            <Button onClick={addTag}>Add</Button>
          </div>

          <div className="flex flex-wrap gap-2 mt-4">
            {form.labels.map((label) => (
              <Badge key={label} variant="secondary">
                {label}
              </Badge>
            ))}
          </div>
        </div>

        <LocationPicker
          value={{
            latitude: form.latitude,
            longitude: form.longitude,
            city: form.city,
            state: form.state,
            locality: form.locality,
          }}
          onChange={(location) =>
            setForm((prev) => ({
              ...prev,
              ...location,
            }))
          }
        />
      </div>

      <div className="lg:col-span-4">
        <div className="sticky top-24 space-y-4">
          <ListingPreviewCard form={form} />

          <div className="content-surface p-5">
            <Button className="w-full cta-primary" onClick={handleSubmit} disabled={loading}>
              {loading ? "Publishing..." : "Publish Listing"}
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}
