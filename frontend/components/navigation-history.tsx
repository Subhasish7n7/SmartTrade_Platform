"use client";

import {
  createContext,
  useContext,
  useEffect,
  useRef,
} from "react";
import { usePathname, useRouter } from "next/navigation";

const NavigationContext = createContext({
  goBack: () => {},
});

export function NavigationProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();

  const history = useRef<string[]>([]);

  useEffect(() => {
    const stack = history.current;

    if (stack[stack.length - 1] !== pathname) {
      stack.push(pathname);
    }
  }, [pathname]);

  function goBack() {
    const stack = history.current;

    // Remove current page
    stack.pop();

    // Previous page
    const previous = stack.pop();

    if (previous) {
      router.push(previous);
    } else {
      router.push("/");
    }
  }

  return (
    <NavigationContext.Provider value={{ goBack }}>
      {children}
    </NavigationContext.Provider>
  );
}

export function useNavigationHistory() {
  return useContext(NavigationContext);
}