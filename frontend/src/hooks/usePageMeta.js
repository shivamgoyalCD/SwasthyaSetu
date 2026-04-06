import { useEffect } from 'react';

export default function usePageMeta(title, description) {
  useEffect(() => {
    document.title = title ? `${title} | SwasthyaSetu` : 'SwasthyaSetu';
  }, [title]);

  useEffect(() => {
    if (!description) {
      return undefined;
    }

    let descriptionTag = document.querySelector('meta[name="description"]');

    if (!descriptionTag) {
      descriptionTag = document.createElement('meta');
      descriptionTag.name = 'description';
      document.head.appendChild(descriptionTag);
    }

    const previousDescription = descriptionTag.content;
    descriptionTag.content = description;

    return () => {
      descriptionTag.content = previousDescription;
    };
  }, [description]);
}
