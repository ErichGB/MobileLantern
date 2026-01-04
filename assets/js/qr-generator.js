// QR Generator with Real QRCode Library and Form Validation
import QRCode from 'qrcode';

class QRGenerator {
  constructor() {
    this.form = null;
    this.downloadButton = null;
    this.canvas = null;
    this.resultContainer = null;
    this.errorContainer = null;
    this.errorMessage = null;
    this.fields = {};
    this.config = null;
    this.currentQRData = null;
    this.debounceTimer = null;
    this.isGenerating = false;
    this.charCounter = {
      text: null,
      remaining: null,
      bar: null,
      warning: null,
      max: 800
    };
    this.requiredFields = ['userName', 'password'];
    this.init();
  }

  async init() {
    this.form = document.getElementById('qr-form');
    this.downloadButton = document.getElementById('download-btn');
    this.canvas = document.getElementById('qr-canvas');
    this.resultContainer = document.getElementById('qr-result');
    this.errorContainer = document.getElementById('qr-error');
    this.errorMessage = document.getElementById('qr-error-message');
    
    // Load configuration
    await this.loadConfig();
    
    // Register basic fields
    this.fields = {
      userName: document.getElementById('userName'),
      password: document.getElementById('password'),
      classroom: document.getElementById('classroom'),
      context: document.getElementById('context')
    };

    // Register dynamic accordion fields - wait a bit for DOM to be ready
    await new Promise(resolve => setTimeout(resolve, 100));
    
    if (this.config && this.config.accordions) {
      this.config.accordions.forEach(accordion => {
        if (accordion.enabled) {
          accordion.fields.forEach(field => {
            const element = document.getElementById(field.name);
            if (element) {
              this.fields[field.name] = element;
            }
          });
        }
      });
    }

    this.charCounter.text = document.getElementById('char-counter-text');
    this.charCounter.remaining = document.getElementById('char-counter-remaining');
    this.charCounter.bar = document.getElementById('char-counter-bar');
    this.charCounter.warning = document.getElementById('char-counter-warning');

    if (!this.form || !this.canvas) {
      console.error('QR Generator: Required elements not found');
      return;
    }
    
    this.setupEventListeners();
    
    // Wait a bit more to ensure all fields are registered, then update
    setTimeout(() => {
      this.updateCharCounter();
      const validation = this.validateForm();
      if (validation.valid) {
        this.clearValidationErrors();
      } else {
        this.showValidationErrors(validation);
      }
    }, 200);
  }

  async loadConfig() {
    try {
      const configElement = document.getElementById('qr-form-config');
      if (configElement) {
        this.config = JSON.parse(configElement.textContent);
      } else {
        console.warn('Could not load config, using defaults');
        this.config = { accordions: [] };
      }
    } catch (error) {
      console.error('Error loading config:', error);
      this.config = { accordions: [] };
    }
  }

  setupEventListeners() {
    this.form.addEventListener('submit', (e) => {
      e.preventDefault();
    });

    // Use event delegation to catch all input events, including dynamic fields
    this.form.addEventListener('input', (e) => {
      // Register field if it's a dynamic accordion field
      if (e.target && e.target.id) {
        const optionalFields = ['classroom', 'context', 'qt', 'qs', 'qp', 'at', 'as', 'ap'];
        if (optionalFields.includes(e.target.id) && !this.fields[e.target.id]) {
          this.fields[e.target.id] = e.target;
        }
      }
      
      // Update counter and validate
      this.updateCharCounter();
      const validation = this.validateForm();
      if (validation.valid) {
        this.clearValidationErrors();
      } else {
        this.showValidationErrors(validation);
      }
      this.debouncedGenerateQR();
    });

    this.form.addEventListener('blur', (e) => {
      if (e.target && (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA')) {
        // Register field if it's a dynamic accordion field
        if (e.target.id) {
          const optionalFields = ['classroom', 'context', 'qt', 'qs', 'qp', 'at', 'as', 'ap'];
          if (optionalFields.includes(e.target.id) && !this.fields[e.target.id]) {
            this.fields[e.target.id] = e.target;
          }
        }
        
        // Update counter and validate
        this.updateCharCounter();
        const validation = this.validateForm();
        if (validation.valid) {
          this.clearValidationErrors();
        } else {
          this.showValidationErrors(validation);
        }
        this.debouncedGenerateQR();
      }
    }, true);

    if (this.downloadButton) {
      this.downloadButton.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        this.downloadQR();
      });
    }

    setTimeout(() => {
      const validation = this.validateForm();
      if (validation.valid) {
        this.clearValidationErrors();
        this.generateQR();
      } else {
        this.showValidationErrors(validation);
      }
    }, 100);
  }

  debouncedGenerateQR() {
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer);
    }

    this.debounceTimer = setTimeout(() => {
      const validation = this.validateForm();
      if (validation.valid && !this.isGenerating) {
        this.clearValidationErrors();
        this.generateQR();
      } else if (!validation.valid) {
        this.showValidationErrors(validation);
      }
    }, 500);
  }

  getQRContentString() {
    const userName = this.fields.userName?.value.trim() || '';
    const password = this.fields.password?.value.trim() || '';
    
    const qrData = { userName, password };
    
    // Add optional fields (only if they have values)
    // Try to get from registered fields first, then from DOM directly
    const optionalFields = ['classroom', 'context', 'qt', 'qs', 'qp', 'at', 'as', 'ap'];
    // Map field names to JSON keys (classroom -> aula)
    const fieldToJsonKey = { classroom: 'aula' };
    optionalFields.forEach(fieldName => {
      let field = this.fields[fieldName];
      // If not registered, try to get from DOM
      if (!field) {
        field = document.getElementById(fieldName);
        if (field) {
          this.fields[fieldName] = field;
        }
      }
      
      if (field) {
        const value = field.value.trim();
        if (value) {
          const jsonKey = fieldToJsonKey[fieldName] || fieldName;
          qrData[jsonKey] = value;
        }
      }
    });
    
    try {
      return JSON.stringify(qrData);
    } catch {
      return '';
    }
  }

  showValidationErrors(validation) {
    // Hide QR result
    if (this.resultContainer) {
      this.resultContainer.classList.add('hidden');
    }

    // Show error container
    if (this.errorContainer) {
      this.errorContainer.classList.remove('hidden');
    }

    // Set error message
    if (this.errorMessage && validation.errors.length > 0) {
      const errorText = validation.errors.map(e => e.message).join('. ');
      this.errorMessage.textContent = errorText;
    }

    // Add error classes to invalid fields
    validation.errors.forEach(error => {
      if (error.field && this.fields[error.field]) {
        this.fields[error.field].classList.remove('border-slate-300', 'dark:border-slate-600');
        this.fields[error.field].classList.add('border-red-500', 'dark:border-red-500');
      }
    });

    // Disable download button
    if (this.downloadButton) {
      this.downloadButton.disabled = true;
    }
  }

  clearValidationErrors() {
    // Hide error container
    if (this.errorContainer) {
      this.errorContainer.classList.add('hidden');
    }

    // Remove error classes from all required fields
    this.requiredFields.forEach(fieldName => {
      if (this.fields[fieldName]) {
        this.fields[fieldName].classList.remove('border-red-500', 'dark:border-red-500');
        this.fields[fieldName].classList.add('border-slate-300', 'dark:border-slate-600');
      }
    });

    // Enable download button
    if (this.downloadButton) {
      this.downloadButton.disabled = false;
    }
  }

  updateCharCounter() {
    if (!this.charCounter.text) return;

    const content = this.getQRContentString();
    const length = content.length;
    const max = this.charCounter.max;
    const percent = Math.min((length / max) * 100, 100);
    const remaining = max - length;

    this.charCounter.text.textContent = `${length} / ${max} caracteres`;
    if (this.charCounter.remaining) {
      if (remaining >= 0) {
        this.charCounter.remaining.textContent = `${remaining} restantes`;
      } else {
        this.charCounter.remaining.textContent = `${Math.abs(remaining)} sobre el límite`;
      }
    }

    if (this.charCounter.bar) {
      this.charCounter.bar.style.width = `${percent}%`;
      let color = '#22c55e';
      if (length > max) {
        color = '#dc2626';
      } else if (length > max * 0.85) {
        color = '#f97316';
      } else if (length > max * 0.6) {
        color = '#facc15';
      }
      this.charCounter.bar.style.backgroundColor = color;
    }

    if (this.charCounter.warning) {
      if (length > max) {
        this.charCounter.warning.classList.remove('hidden');
      } else {
        this.charCounter.warning.classList.add('hidden');
      }
    }
  }

  validateForm() {
    const errors = [];
    const fieldLabels = {
      userName: 'Usuario',
      password: 'Contraseña',
      context: 'Contexto Pedagógico'
    };

    // Check required fields
    this.requiredFields.forEach(fieldName => {
      const field = this.fields[fieldName];
      if (!field || !field.value.trim()) {
        errors.push({
          field: fieldName,
          message: `El campo "${fieldLabels[fieldName]}" es requerido`
        });
      }
    });

    // Check character limit
    const qrContent = this.getQRContentString();
    if (qrContent.length > this.charCounter.max) {
      errors.push({
        field: null,
        message: `El contenido excede el límite de ${this.charCounter.max} caracteres`
      });
    }

    return {
      valid: errors.length === 0,
      errors: errors
    };
  }

  async generateQR() {
    const validation = this.validateForm();
    if (!validation.valid || this.isGenerating) {
      if (!validation.valid) {
        this.showValidationErrors(validation);
      }
      return;
    }

    this.clearValidationErrors();
    this.isGenerating = true;

    try {
      const qrData = {
        userName: this.fields.userName.value.trim(),
        password: this.fields.password.value.trim()
      };

      // Add optional fields (only if they have values)
      const optionalFields = ['classroom', 'context', 'qt', 'qs', 'qp', 'at', 'as', 'ap'];
      // Map field names to JSON keys (classroom -> aula)
      const fieldToJsonKey = { classroom: 'aula' };
      optionalFields.forEach(fieldName => {
        if (this.fields[fieldName]) {
          const value = this.fields[fieldName].value.trim();
          if (value) {
            const jsonKey = fieldToJsonKey[fieldName] || fieldName;
            qrData[jsonKey] = value;
          }
        }
      });

      const qrContent = JSON.stringify(qrData);
      
      if (this.currentQRData === qrContent) {
        return;
      }
      
      this.currentQRData = qrContent;

      const qrCodeDataURL = await QRCode.toDataURL(qrContent, {
        width: 200,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        },
        errorCorrectionLevel: 'M'
      });

      await this.drawQRToCanvas(qrCodeDataURL);

      if (this.resultContainer) {
        this.resultContainer.classList.remove('hidden');
      }

      if (this.errorContainer) {
        this.errorContainer.classList.add('hidden');
      }
      
    } catch (error) {
      console.error('Error generating QR code:', error);
    } finally {
      this.isGenerating = false;
    }
  }

  async drawQRToCanvas(dataURL) {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => {
        const ctx = this.canvas.getContext('2d');
        
        this.canvas.width = 200;
        this.canvas.height = 200;
        
        ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        ctx.drawImage(img, 0, 0, 200, 200);
        
        resolve();
      };
      img.onerror = reject;
      img.src = dataURL;
    });
  }

  downloadQR() {
    if (!this.canvas || !this.currentQRData) {
      return;
    }

    try {
      const timestamp = Date.now();
      const filename = `mobilelanter-qr-${timestamp}.png`;

      const link = document.createElement('a');
      link.download = filename;
      link.href = this.canvas.toDataURL('image/png');
      link.style.display = 'none';

      document.body.appendChild(link);
      link.click();

      setTimeout(() => {
        if (document.body.contains(link)) {
          document.body.removeChild(link);
        }
      }, 100);
    } catch (error) {
      console.error('Error downloading QR code:', error);
      alert('Error al descargar el código QR.');
    }
  }
}

// Auto-initialize when DOM is ready
if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      new QRGenerator();
    });
  } else {
    new QRGenerator();
  }
}

export { QRGenerator };

