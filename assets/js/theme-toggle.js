// Theme Toggle with Button Group
class ThemeToggle {
  constructor() {
    this.themes = ['light', 'dark', 'auto'];
    this.currentTheme = 'auto';
    this.mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    this.init();
  }

  init() {
    this.container = document.getElementById('theme-toggle-group');
    
    if (!this.container) {
      console.error('Theme toggle container not found');
      return;
    }

    // Set initial theme
    this.setInitialTheme();
    
    // Add event listeners to buttons
    this.container.addEventListener('click', (e) => {
      const button = e.target.closest('button[data-theme]');
      if (button && button.dataset.theme) {
        this.setTheme(button.dataset.theme);
      }
    });
    
    // Listen for system theme changes ONLY when in auto mode
    this.mediaQuery.addEventListener('change', (e) => {
      if (this.currentTheme === 'auto') {
        this.applyTheme();
      }
    });
  }

  setInitialTheme() {
    const savedTheme = localStorage.getItem('theme') || 'auto';
    this.setTheme(savedTheme);
  }

  setTheme(theme) {
    if (!this.themes.includes(theme)) {
      return;
    }

    this.currentTheme = theme;
    localStorage.setItem('theme', theme);
    this.updateButtons();
    this.applyTheme();
  }

  updateButtons() {
    if (!this.container) return;

    const buttons = this.container.querySelectorAll('button[data-theme]');
    
    buttons.forEach(button => {
      const isActive = button.dataset.theme === this.currentTheme;
      
      if (isActive) {
        button.classList.add('bg-slate-900', 'text-white', 'dark:bg-white', 'dark:text-slate-900');
        button.classList.remove('text-slate-500', 'hover:text-slate-700', 'dark:text-slate-400', 'dark:hover:text-slate-200');
      } else {
        button.classList.remove('bg-slate-900', 'text-white', 'dark:bg-white', 'dark:text-slate-900');
        button.classList.add('text-slate-500', 'hover:text-slate-700', 'dark:text-slate-400', 'dark:hover:text-slate-200');
      }
    });
  }

  applyTheme() {
    let shouldBeDark = false;
    
    if (this.currentTheme === 'dark') {
      shouldBeDark = true;
    } else if (this.currentTheme === 'light') {
      shouldBeDark = false;
    } else if (this.currentTheme === 'auto') {
      shouldBeDark = this.mediaQuery.matches;
    }

    document.documentElement.classList.remove('dark');
    
    if (shouldBeDark) {
      document.documentElement.classList.add('dark');
    }
  }

  // Method for keyboard shortcuts
  toggleTheme() {
    const currentIndex = this.themes.indexOf(this.currentTheme);
    const nextIndex = (currentIndex + 1) % this.themes.length;
    this.setTheme(this.themes[nextIndex]);
  }
}

// Initialize theme toggle when DOM is loaded
if (typeof window !== 'undefined') {
  document.addEventListener('DOMContentLoaded', () => {
    window.themeToggle = new ThemeToggle();
    
    // Add keyboard shortcuts
    document.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 't') {
        e.preventDefault();
        if (window.themeToggle) {
          window.themeToggle.toggleTheme();
        }
      }
    });
  });
}

export { ThemeToggle };

