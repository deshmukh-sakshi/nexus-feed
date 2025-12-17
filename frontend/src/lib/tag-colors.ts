/**
 * Generate a deterministic hash from a string using FNV-1a algorithm.
 * Much better distribution for short strings.
 */
const hashString = (str: string): number => {
  let hash = 2166136261 // FNV offset basis
  for (let i = 0; i < str.length; i++) {
    hash ^= str.charCodeAt(i)
    hash = Math.imul(hash, 16777619) // FNV prime
  }
  return hash >>> 0 // Convert to unsigned 32-bit
}

/**
 * Generate a deterministic solid color based on tag name.
 * Same tag name always produces the same color.
 */
export const generateTagColor = (tagName: string): string => {
  if (!tagName) return 'hsl(0, 0%, 50%)'

  const hash = hashString(tagName.toLowerCase())
  
  // Use golden ratio to spread hues evenly
  const hue = (hash * 137.508) % 360

  // Use fixed saturation and lightness for good visibility
  return `hsl(${Math.round(hue)}, 65%, 45%)`
}

/**
 * Get a contrasting text color (black or white) for a given tag color.
 */
export const getTagTextColor = (tagName: string): string => {
  if (!tagName) return '#000'

  const hash = hashString(tagName.toLowerCase())
  const hue = (hash * 137.508) % 360
  
  // Yellow-green hues (45-170) need dark text at 45% lightness
  if (hue >= 45 && hue <= 170) {
    return '#000'
  }
  return '#fff'
}
