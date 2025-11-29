const CLOUDINARY_CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME
const CLOUDINARY_UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET

export interface CloudinaryUploadResponse {
  secure_url: string
  public_id: string
  width: number
  height: number
  format: string
  bytes: number
}

export interface UploadProgress {
  loaded: number
  total: number
  percent: number
}

export const uploadToCloudinary = async (
  file: File,
  onProgress?: (progress: UploadProgress) => void
): Promise<CloudinaryUploadResponse> => {
  if (!CLOUDINARY_CLOUD_NAME || !CLOUDINARY_UPLOAD_PRESET) {
    throw new Error('Cloudinary configuration missing. Please set VITE_CLOUDINARY_CLOUD_NAME and VITE_CLOUDINARY_UPLOAD_PRESET in your .env file')
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET)

  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    
    xhr.upload.addEventListener('progress', (event) => {
      if (event.lengthComputable && onProgress) {
        onProgress({
          loaded: event.loaded,
          total: event.total,
          percent: Math.round((event.loaded / event.total) * 100),
        })
      }
    })

    xhr.addEventListener('load', () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          const response = JSON.parse(xhr.responseText)
          resolve(response)
        } catch {
          reject(new Error('Failed to parse Cloudinary response'))
        }
      } else {
        reject(new Error(`Upload failed with status ${xhr.status}`))
      }
    })

    xhr.addEventListener('error', () => {
      reject(new Error('Network error during upload'))
    })

    xhr.open('POST', `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`)
    xhr.send(formData)
  })
}

export const uploadMultipleToCloudinary = async (
  files: File[],
  onProgress?: (fileIndex: number, progress: UploadProgress) => void
): Promise<CloudinaryUploadResponse[]> => {
  const results: CloudinaryUploadResponse[] = []
  
  for (let i = 0; i < files.length; i++) {
    const result = await uploadToCloudinary(files[i], (progress) => {
      onProgress?.(i, progress)
    })
    results.push(result)
  }
  
  return results
}
