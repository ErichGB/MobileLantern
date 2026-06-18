// Download QR: renders a QR code for the latest Android APK download URL.
import QRCode from 'qrcode';

/**
 * Render a QR code for the given URL into a canvas element.
 * @param {HTMLCanvasElement} canvas
 * @param {string} url
 */
async function renderDownloadQR(canvas, url) {
  if (!canvas || !url) return;

  try {
    const isDark = document.documentElement.classList.contains('dark');
    const dataURL = await QRCode.toDataURL(url, {
      width: 200,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      },
      errorCorrectionLevel: 'M'
    });

    await new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => {
        const ctx = canvas.getContext('2d');
        canvas.width = 200;
        canvas.height = 200;
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(img, 0, 0, 200, 200);
        resolve();
      };
      img.onerror = reject;
      img.src = dataURL;
    });
  } catch (error) {
    console.error('Error generating download QR code:', error);
  }
}

// Expose globally so Alpine inline handlers can call it.
if (typeof window !== 'undefined') {
  window.renderDownloadQR = renderDownloadQR;
}

export { renderDownloadQR };

