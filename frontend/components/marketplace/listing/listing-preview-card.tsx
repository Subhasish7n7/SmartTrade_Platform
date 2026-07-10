"use client"

import Image from "next/image"

import { Badge } from "@/components/ui/badge"

interface Props {
  form: {
    itemName: string
    userPrice: string
    description: string
    category: string
    condition: string

    imageUrls: string[]

    city: string
    state: string
    locality: string
  }
}

export function ListingPreviewCard({
  form,
}: Props) {
  const image =
    form.imageUrls[0]

  return (
    <div className="trade-card rounded-[28px] overflow-hidden">

      <div className="trade-card-accent" />

      <div className="p-5">

        <h2 className="font-semibold mb-4">
          Live Preview
        </h2>

        <div className="space-y-4">

          <div className="relative aspect-video rounded-xl overflow-hidden bg-muted">

            {image ? (
              <Image
                src={image}
                alt="preview"
                fill
                className="object-cover"
              />
            ) : (
              <div className="absolute inset-0 flex items-center justify-center text-muted-foreground">
                No Image Selected
              </div>
            )}

          </div>

          <div>
            <h3 className="font-semibold text-lg line-clamp-2">
              {form.itemName || "Item Name"}
            </h3>

            <div className="flex flex-wrap gap-2 mt-3">

              {form.category && (
                <Badge>
                  {form.category}
                </Badge>
              )}

              {form.condition && (
                <Badge variant="outline">
                  {form.condition}
                </Badge>
              )}

            </div>
          </div>

          <div className="trade-stat">

            <p className="text-xs text-muted-foreground">
              Expected Value
            </p>

            <p className="font-semibold text-lg">
              ₹{form.userPrice || "0"}
            </p>

          </div>

          {(form.locality ||
            form.city ||
            form.state) && (
            <div className="trade-stat">

              <p className="text-xs text-muted-foreground">
                Location
              </p>

              <p className="font-medium">
                {[form.locality, form.city]
                  .filter(Boolean)
                  .join(", ")}
              </p>

              <p className="text-sm text-muted-foreground">
                {form.state}
              </p>

            </div>
          )}

          <div>

            <p className="text-xs text-muted-foreground mb-2">
              Description
            </p>

            <p className="text-sm text-muted-foreground line-clamp-5">
              {form.description ||
                "Your item description will appear here."}
            </p>

          </div>

        </div>

      </div>

    </div>
  )
}