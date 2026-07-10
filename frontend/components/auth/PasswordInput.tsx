"use client";

import { Eye, EyeOff } from "lucide-react";

import { Input } from "@/components/ui/input";

import { useState } from "react";

interface Props extends React.InputHTMLAttributes<HTMLInputElement> {}

export function PasswordInput(props: Props) {
  const [show, setShow] = useState(false);

  return (
    <div className="relative">
      <Input {...props} type={show ? "text" : "password"} />

      <button
        type="button"
        onClick={() => setShow(!show)}
        className="absolute right-3 top-1/2 -translate-y-1/2"
      >
        {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
      </button>
    </div>
  );
}
